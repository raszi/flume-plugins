package hu.ssh.flume.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.cloudera.flume.conf.Context;
import com.cloudera.flume.conf.SourceFactory.SourceBuilder;
import com.cloudera.flume.core.Event;
import com.cloudera.flume.core.EventImpl;
import com.cloudera.flume.core.EventSource;
import com.cloudera.util.Pair;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

/**
 * This Flume source reads from a UNIX FIFO.
 *
 * @author KARASZI Istvan (github@spam.raszi.hu)
 */
public class FifoSource extends EventSource.Base {
	/**
	 * How many milliseconds should the source wait if FIFO is empty.
	 */
	private static final long WAIT_TIME = Long.parseLong(System.getProperty("flumePlugins.fifoSource.waitTime", "3000"));

	/**
	 * Describes how to use the source.
	 */
	private static final String USAGE = "usage: fifoSource [fifo]";

	/**
	 * The {@link SourceBuilder} for this source.
	 */
	private static final SourceBuilder BUILDER = new SourceBuilder() {
		@Override
		public EventSource build(final Context ctx, final String... argv) {
			Preconditions.checkArgument(argv.length == 1, USAGE);
			Preconditions.checkArgument(!Strings.isNullOrEmpty(argv[0]), USAGE);

			return new FifoSource(new File(argv[0]));
		}
	};

	/**
	 * The logger.
	 */
	//private static final Logger LOG = LoggerFactory.getLogger(FifoSource.class);

	/**
	 * The file to read from.
	 */
	private final File file;

	/**
	 * The reader.
	 */
	private BufferedReader reader;

	/**
	 * @param file
	 *            the file to read events from
	 */
	public FifoSource(final File file) {
		this.file = Preconditions.checkNotNull(file, "file should be not null");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void open() throws IOException {
		// Initialized the source
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Event next() throws IOException {
		// Next returns the next event, blocking if none available.
		while (true) {
			final String line = reader.readLine();

			if (line != null) {
				return new EventImpl(line.getBytes());
			}

			try {
				Thread.sleep(WAIT_TIME);
				continue;
			} catch (InterruptedException e) {
				throw new IOException("Interrupted", e);
			}
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException {
		// Cleanup
		reader.close();
	}

	/**
	 * This is a special function used by the SourceFactory to pull in this class
	 * as a plugin source.
	 *
	 * @return the source builders
	 */
	public static List<Pair<String, SourceBuilder>> getSourceBuilders() {
		return ImmutableList.of(new Pair<String, SourceBuilder>("fifoSource", BUILDER));
	}
}
