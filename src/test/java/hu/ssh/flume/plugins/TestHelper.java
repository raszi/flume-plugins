package hu.ssh.flume.plugins;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudera.flume.core.Event;
import com.cloudera.flume.core.EventSource;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Iterables;

/**
 * This is a helper class for the test classes.
 *
 * @author KARASZI Istvan (github@spam.raszi.hu)
 */
public class TestHelper {
	/**
	 * The logger instance.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(TestHelper.class);

	/**
	 * @param params
	 *            the data provider parameters
	 * @return the iterator as TestNG needs it
	 */
	public static Iterator<Object[]> convertParams(final List<? extends Iterable<?>> params) {
		final Builder<Object[]> builder = ImmutableList.builder();

		for (final Iterable<?> param : params) {
			builder.add(Iterables.toArray(param, Object.class));
		}

		return builder.build().iterator();
	}

	/**
	 * Reads the source for the selected amount of messages.
	 *
	 * @param source
	 *            the source to use
	 * @param messages
	 *            the number of messages to read
	 * @return the read messages
	 * @throws IOException
	 *             if the source thrown it
	 * @throws InterruptedException
	 *             if the source thrown it
	 */
	public static List<String> readSource(final EventSource.Base source, final int messages)
			throws IOException, InterruptedException
	{
		source.open();

		final Builder<String> builder = ImmutableList.builder();
		try {
			for (int i = 0; i < messages; i++) {
				final Event event = source.next();

				LOG.info("Read {}", event);

				builder.add(new String(event.getBody()));
			}
		} finally {
			source.close();
		}

		return builder.build();
	}
}
