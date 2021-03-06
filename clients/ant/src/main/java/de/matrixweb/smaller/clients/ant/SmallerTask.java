package de.matrixweb.smaller.clients.ant;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import de.matrixweb.smaller.clients.common.ExecutionException;
import de.matrixweb.smaller.clients.common.Logger;
import de.matrixweb.smaller.clients.common.Util;

/**
 * @author marwol
 */
public class SmallerTask extends Task {

  private String processor;

  private String in;

  private String out;

  private String options = "";

  private String host = "sr.s2.de";

  private String port = "80";

  private FileSet files;

  private File target;

  private boolean debug = false;

  /**
   * @param processor
   *          the processor to set
   */
  public final void setProcessor(final String processor) {
    this.processor = processor;
  }

  /**
   * @param in
   *          the in to set
   */
  public final void setIn(final String in) {
    this.in = in;
  }

  /**
   * @param out
   *          the out to set
   */
  public final void setOut(final String out) {
    this.out = out;
  }

  /**
   * @param options
   *          the options to set
   */
  public void setOptions(final String options) {
    this.options = options;
  }

  /**
   * @param host
   *          the host to set
   */
  public final void setHost(final String host) {
    this.host = host;
  }

  /**
   * @param port
   *          the port to set
   */
  public final void setPort(final String port) {
    this.port = port;
  }

  /**
   * @param files
   *          the files to set
   */
  public final void setFiles(final FileSet files) {
    this.files = files;
  }

  /**
   * @param files
   */
  public final void addFileset(final FileSet files) {
    if (this.files != null) {
      throw new BuildException("Only one fileset is allowed");
    }
    this.files = files;
  }

  /**
   * @param target
   *          the target to set
   */
  public final void setTarget(final File target) {
    this.target = target;
  }

  /**
   * @param debug
   *          the debug to set
   */
  public final void setDebug(final boolean debug) {
    this.debug = debug;
  }

  /**
   * @see org.apache.tools.ant.Task#execute()
   */
  public void execute() {
    try {
      final Util util = new Util(new AntLogger(), this.debug);

      final DirectoryScanner ds = this.files.getDirectoryScanner();
      util.unzip(this.target, util.send(this.host, this.port, util.zip(
          ds.getBasedir(), ds.getIncludedFiles(), this.processor, this.in,
          this.out, this.options)));
    } catch (final ExecutionException e) {
      throw new BuildException("Failed execute smaller", e);
    }
  }

  private class AntLogger implements Logger {

    /**
     * @see de.matrixweb.smaller.clients.common.Logger#debug(java.lang.String)
     */
    public void debug(final String message) {
      log(message, Project.MSG_INFO);
    }

  }

}
