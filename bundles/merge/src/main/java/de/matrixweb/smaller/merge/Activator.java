package de.matrixweb.smaller.merge;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import de.matrixweb.smaller.resource.Processor;

/**
 * @author markusw
 */
public class Activator implements BundleActivator {

  private ServiceRegistration<Processor> registration;

  /**
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(final BundleContext context) {
    final Hashtable<String, Object> props = new Hashtable<String, Object>();
    props.put("name", "merge");
    this.registration = context.registerService(Processor.class,
        new MergeProcessor(), props);
  }

  /**
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(final BundleContext context) {
    if (this.registration != null) {
      this.registration.unregister();
      this.registration = null;
    }
  }

}
