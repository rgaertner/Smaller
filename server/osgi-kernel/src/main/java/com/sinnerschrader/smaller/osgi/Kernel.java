package com.sinnerschrader.smaller.osgi;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import com.sinnerschrader.smaller.osgi.maven.MavenInstaller;
import com.sinnerschrader.smaller.osgi.maven.impl.MavenInstallerImpl;
import com.sinnerschrader.smaller.osgi.maven.impl.MavenInstallerImpl.BundleTask;

/**
 * @author markusw
 */
public class Kernel {

  /**
   * @param args
   */
  public static void main(String... args) {
    new Kernel().start(args);
  }

  private void start(String... args) {
    HashMap<String, String> config = new HashMap<String, String>();
    config.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA,
        "com.sinnerschrader.smaller.osgi.maven");
    Framework framework = ServiceLoader.load(FrameworkFactory.class).iterator()
        .next().newFramework(config);
    try {
      framework.start();
      run(framework, args);
    } catch (BundleException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Throwable t) {
      t.printStackTrace();
    } finally {
      System.exit(0);
    }
  }

  private String getRepository(String... args) {
    String repository = null;
    for (String arg : args) {
      if (arg.startsWith("-repository=")) {
        repository = arg.substring("-repository=".length());
        if (repository.endsWith("/")) {
          repository = repository.substring(0, repository.length() - 1);
        }
        return repository;
      }
    }
    throw new RuntimeException("Missing 'repository' parameter");
  }

  private void run(Framework framework, String... args) throws IOException,
      InterruptedException {
    MavenInstallerImpl maven = new MavenInstallerImpl(getRepository(args),
        framework);
    framework.getBundleContext().registerService(MavenInstaller.class, maven,
        null);
    installBundles(maven, args);
    framework.waitForStop(0);
  }

  private void installBundles(MavenInstallerImpl maven, String... args)
      throws IOException {
    try {
      Set<BundleTask> tasks = new HashSet<MavenInstallerImpl.BundleTask>();
      for (String arg : args) {
        if (arg.startsWith("mvn:")) {
          tasks.addAll(maven.install(arg));
        }
      }
      maven.startOrUpdate(tasks, false);
    } catch (BundleException e) {
      e.printStackTrace();
    }
  }

}
