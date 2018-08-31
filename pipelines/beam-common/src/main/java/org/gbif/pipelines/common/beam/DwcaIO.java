package org.gbif.pipelines.common.beam;

import org.gbif.pipelines.io.avro.ExtendedRecord;
import org.gbif.pipelines.parsers.io.DwcaReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.io.BoundedSource;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.display.DisplayData;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;

/**
 * IO operations for DwC-A formats.
 *
 * <p>Provides the ability to read a DwC-A as a bounded source, but in a non splittable manner. This
 * means that a single threaded approach to reading is enforced.
 *
 * <p>This is intended only for demonstration usage, and not for production.
 *
 * <p>To use this:
 *
 * <pre>{@code
 * p.apply("read",
 *     DwcaIO.Read.withPaths("/tmp/my-dwca.zip", "/tmp/working")
 *     ...;
 * }</pre>
 */
public class DwcaIO {

  public static class Read extends PTransform<PBegin, PCollection<ExtendedRecord>> {

    private static final String UNCOMPRESSED = "UNCOMPRESSED";

    private final String path;
    private final String workingPath;

    /**
     * Reads an expanded/uncompressed DwCA content
     *
     * @param working path to an expanded/uncompressed DwCA content
     */
    public static Read withPaths(String working) {
      return new Read(working);
    }

    /**
     * Reads a DwCA archive and stores uncompressed DwCA content to a working directory
     *
     * @param file path to a DwCA archive
     * @param working path to a directory for storing uncompressed DwCA content
     */
    public static Read withPaths(String file, String working) {
      return new Read(file, working);
    }

    /**
     * Reads an expanded/uncompressed DwCA content
     *
     * @param workingPath path to an expanded/uncompressed DwCA content
     */
    private Read(String workingPath) {
      this(UNCOMPRESSED, workingPath);
    }

    /**
     * Reads a DwCA archive and stores uncompressed DwCA content to a working directory
     *
     * @param filePath path to a DwCA archive
     * @param workingPath path to a directory for storing uncompressed DwCA content
     */
    private Read(String filePath, String workingPath) {
      path = filePath;
      this.workingPath = workingPath;
    }

    @Override
    public PCollection<ExtendedRecord> expand(PBegin input) {
      DwCASource source = new DwCASource(this);
      return input.getPipeline().apply(org.apache.beam.sdk.io.Read.from(source));
    }

    @Override
    public void populateDisplayData(DisplayData.Builder builder) {
      super.populateDisplayData(builder);
      builder.add(DisplayData.item("DwC-A Path", path));
    }
  }

  /** A non-splittable bounded source. */
  private static class DwCASource extends BoundedSource<ExtendedRecord> {

    private final Read read;

    DwCASource(Read read) {
      this.read = read;
    }

    @Override
    public Coder<ExtendedRecord> getOutputCoder() {
      return AvroCoder.of(ExtendedRecord.class);
    }

    /** Will always return a single entry list of just ourselves. This is not splittable. */
    @Override
    public List<? extends BoundedSource<ExtendedRecord>> split(
        long desiredBundleSizeBytes, PipelineOptions options) {
      List<DwCASource> readers = new ArrayList<>();
      readers.add(this);
      return readers;
    }

    @Override
    public long getEstimatedSizeBytes(PipelineOptions options) {
      return 0; // unknown
    }

    @Override
    public BoundedReader<ExtendedRecord> createReader(PipelineOptions options) {
      return new BoundedDwCAReader(this);
    }
  }

  /** A wrapper around the standard DwC-IO provided NormalizedDwcArchive. */
  private static class BoundedDwCAReader extends BoundedSource.BoundedReader<ExtendedRecord> {

    private final DwCASource source;
    private DwcaReader reader;

    private BoundedDwCAReader(DwCASource source) {
      this.source = source;
    }

    @Override
    public boolean start() throws IOException {
      if (Read.UNCOMPRESSED.equals(source.read.path)) {
        reader = new DwcaReader(source.read.workingPath);
      } else {
        reader = new DwcaReader(source.read.path, source.read.workingPath);
      }

      return reader.init();
    }

    @Override
    public boolean advance() {
      return reader.advance();
    }

    @Override
    public ExtendedRecord getCurrent() {
      return reader.getCurrent();
    }

    @Override
    public void close() throws IOException {
      reader.close();
    }

    @Override
    public BoundedSource<ExtendedRecord> getCurrentSource() {
      return source;
    }
  }
}
