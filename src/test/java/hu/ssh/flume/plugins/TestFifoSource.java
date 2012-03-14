package hu.ssh.flume.plugins;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;

/**
 * This class tests the {@link FifoSource}.
 *
 * @author KARASZI Istvan (github@spam.raszi.hu)
 */
public class TestFifoSource {
	/**
	 * The logger instance.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(TestFifoSource.class);

	/**
	 * The random generator.
	 */
	static final ThreadLocal<Random> RANDOM = new ThreadLocal<Random>() {
		@Override
		protected Random initialValue() {
			return new Random(System.currentTimeMillis());
		}
	};

	protected static final double WAIT_BETWEEN_WRITES = 200;

	/**
	 * The logger instance.
	 */
	final Logger log = LoggerFactory.getLogger(TestFifoSource.class);

	@DataProvider
	Iterator<Object[]> testData() {
		return TestHelper.convertParams(ImmutableList.of(ImmutableList.of(
				ImmutableList.of("test0", "test1", "test3", "test4", "test5"))));
	}

	/**
	 * Tests the {@link FifoSource}.
	 * <p>
	 * Creates a temporary UNIX FIFO file, writes messages to it and reads back w/
	 * {@link FifoSource}.
	 *
	 * @param messages
	 *            the messages to write
	 * @throws Exception
	 *             if any error occurred
	 */
	@Test(dataProvider = "testData")
	public void testFifo(final List<String> messages) throws Exception {
		final File fifo = createFifo();

		final FifoSource source = new FifoSource(fifo);

		final Thread writer = new Thread(createWriter(fifo, messages), "writer");
		writer.start();

		final List<String> read = TestHelper.readSource(source, messages.size());
		Assert.assertEquals(read, messages, "The read messages should be the same as the written");

		writer.join();
		Assert.assertTrue(fifo.delete(), "Fifo should be deleted");
	}

	/**
	 * @param fifo
	 * @param messages
	 * @return
	 */
	private Runnable createWriter(final File fifo, final List<String> messages) {
		return new Runnable() {
			@Override
			public void run() {
				final BufferedOutputStream os;
				try {
					os = new BufferedOutputStream(new FileOutputStream(fifo));
				} catch (FileNotFoundException e) {
					LOG.warn("Could not open file", e);
					return;
				}

				try {
					for (final String message : messages) {
						final long waitTime = Math.round(RANDOM.get().nextDouble() * WAIT_BETWEEN_WRITES);
						Thread.sleep(waitTime);

						LOG.info("Writing {}", message);

						os.write(message.concat("\n").getBytes());
						os.flush();
					}
				} catch (final IOException e) {
					LOG.warn("Could not write the file", e);
				} catch (final InterruptedException e) {
					LOG.warn("Interrupted", e);
				} finally {
					try {
						os.close();
					} catch (final IOException e) {
						LOG.warn("Could not close the file", e);
					}
				}
			}
		};
	}

	/**
	 * Creates a temporary UNIX FIFO.
	 *
	 * @return the file pointing to the FIFO
	 * @throws IOException
	 *             if the temporary file could not be created
	 * @throws InterruptedException
	 *             if the FIFO could not be created
	 */
	private File createFifo() throws IOException, InterruptedException {
		final File fifo = File.createTempFile("test-", ".fifo");
		Assert.assertTrue(fifo.delete(), "Temporary file should be deleted");

		log.debug("Creating fifo at {}", fifo);

		final Process exec = Runtime.getRuntime().exec(String.format("mkfifo %s", fifo));
		Assert.assertEquals(0, exec.waitFor(), "Fifo should be created");

		return fifo;
	}
}
