package de.matrixweb.smaller.lessjs;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import de.matrixweb.smaller.common.SmallerException;
import de.matrixweb.smaller.javascript.JavaScriptExecutor;
import de.matrixweb.smaller.resource.Processor;
import de.matrixweb.smaller.resource.Resource;
import de.matrixweb.smaller.resource.ResourceResolver;
import de.matrixweb.smaller.resource.StringResource;
import de.matrixweb.smaller.resource.Type;

/**
 * @author markusw
 */
public class LessjsProcessor implements Processor {

  private final ProxyResourceResolver proxy = new ProxyResourceResolver();

  private final JavaScriptExecutor executor;

  /**
   * 
   */
  public LessjsProcessor() {
    this("1.3.0");
  }

  /**
   * @param version
   */
  public LessjsProcessor(final String version) {
    this.executor = new JavaScriptExecutor("less-" + version, getClass());
    // this.executor.addProperty("resolver", this.proxy);
    this.executor.addGlobalFunction("resolve", new ResolveFunctor(this.proxy));
    this.executor.addScriptFile(getClass().getResource("/lessjs/less-env.js"));
    this.executor.addScriptFile(getClass().getResource(
        "/lessjs/less-" + version + ".js"));
    this.executor.addCallScript("lessIt(%s);");
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#supportsType(de.matrixweb.smaller.resource.Type)
   */
  @Override
  public boolean supportsType(final Type type) {
    return type == Type.CSS;
  }

  /**
   * @see de.matrixweb.smaller.resource.Processor#execute(de.matrixweb.smaller.resource.Resource,
   *      java.util.Map)
   */
  @Override
  public Resource execute(final Resource resource,
      final Map<String, String> options) throws IOException {
    final StringWriter writer = new StringWriter();

    this.proxy.setResolver(resource.getResolver());
    try {
      this.executor.run(new StringReader(resource.getContents()), writer);
    } finally {
      this.proxy.removeResolver();
    }

    return new StringResource(resource.getResolver(), resource.getType(),
        resource.getPath(), writer.toString());
  }

  /** */
  public static class ResolveFunctor {

    private final ResourceResolver resolver;

    private ResolveFunctor(final ResourceResolver resolver) {
      this.resolver = resolver;
    }

    /**
     * @param input
     * @return Returns the resolved {@link Resource} content
     */
    public String resolve(final String input) {
      try {
        return this.resolver.resolve(input).getContents();
      } catch (final IOException e) {
        throw new SmallerException(
            "Failed to resolve resource '" + input + "'", e);
      }
    }

  }

  private static class ProxyResourceResolver implements ResourceResolver {

    private final ThreadLocal<ResourceResolver> resolver = new ThreadLocal<ResourceResolver>();

    /**
     * @see de.matrixweb.smaller.resource.ResourceResolver#resolve(java.lang.String)
     */
    @Override
    public Resource resolve(final String path) {
      return this.resolver.get().resolve(path);
    }

    private void setResolver(final ResourceResolver resolver) {
      this.resolver.set(resolver);
    }

    private void removeResolver() {
      this.resolver.remove();
    }

  }

}
