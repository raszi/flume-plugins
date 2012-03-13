package flumePlugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudera.flume.conf.Context;
import com.cloudera.flume.conf.SourceFactory.SourceBuilder;
import com.cloudera.flume.core.Event;
import com.cloudera.flume.core.EventImpl;
import com.cloudera.flume.core.EventSource;
import com.cloudera.util.Pair;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * This Flume source reads from a UNIX FIFO.
 * 
 * @author KARASZI Istvan (github@spam.raszi.hu)
 */
public class FifoSource extends EventSource.Base {
	static final Logger LOG = LoggerFactory.getLogger(FifoSource.class);

	/**
	 * The FIFO.
	 */
	private final File file;

	/**
	 * The reader.
	 */
	private BufferedReader reader;

	/**
	 * @param file
	 */
	public FifoSource(final File file) {
		this.file = Preconditions.checkNotNull(file);
	}

	@Override
	public void open() throws IOException {
		// Initialized the source
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
	}

	@Override
	public Event next() throws IOException {
		// Next returns the next event, blocking if none available.
		return new EventImpl(reader.readLine().getBytes());
	}

	@Override
	public void close() throws IOException {
		// Cleanup
		reader.close();
	}

	private static SourceBuilder builder() {
		// construct a new parameterized source
		return new SourceBuilder() {
			@Override
			public EventSource build(Context ctx, String... argv) {
				Preconditions.checkArgument(argv.length == 1, "usage: fifoSource [fifo]");
				Preconditions.checkArgument(!Strings.isNullOrEmpty(argv[0]), "usage: fifoSource [fifo]");

				return new FifoSource(new File(argv[0]));
			}
		};
	}

	/**
	 * This is a special function used by the SourceFactory to pull in this class
	 * as a plugin source.
	 *
	 * @return the source builders
	 */
	public static List<Pair<String, SourceBuilder>> getSourceBuilders() {
		final List<Pair<String, SourceBuilder>> builders = new ArrayList<Pair<String, SourceBuilder>>();

		builders.add(new Pair<String, SourceBuilder>("fifoSource", builder()));

		return builders;
	}
}
