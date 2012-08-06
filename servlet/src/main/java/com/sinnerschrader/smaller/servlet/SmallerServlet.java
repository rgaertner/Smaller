package com.sinnerschrader.smaller.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.sinnerschrader.smaller.common.Task;
import com.sinnerschrader.smaller.lib.ProcessorChain;
import com.sinnerschrader.smaller.lib.Result;

/**
 * @author marwol
 */
public class SmallerServlet extends HttpServlet {

  private static final long serialVersionUID = -7622385125982337921L;

  private Result result;

  /**
   * @see javax.servlet.GenericServlet#init()
   */
  @Override
  public void init() throws ServletException {
    String processors = getInitParameter("processors");
    if (processors == null) {
      throw new ServletException("init-param 'processors' must be configured");
    }
    String includes = getInitParameter("includes");
    if (StringUtils.isBlank(includes)) {
      throw new ServletException("init-param 'includes' must be configured");
    }
    String excludes = getInitParameter("excludes");
    Set<String> resources = new ResourceScanner(getServletContext(),
        includes.split("[, ]"), excludes != null ? excludes.split("[, ]")
            : new String[] {}).getResources();

    Task task = new Task();
    task.setProcessor(processors);
    task.setIn(resources.toArray(new String[resources.size()]));
    this.result = new ProcessorChain().execute(
        new ServletContextResourceResolver(getServletContext()), task);
  }

  /**
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doGet(final HttpServletRequest request,
      final HttpServletResponse response) throws ServletException, IOException {
    String contentType = request.getContentType();
    if (contentType == null) {
      if (request.getRequestURI().endsWith("js")) {
        contentType = "text/javascript";
      } else if (request.getRequestURI().endsWith("css")) {
        contentType = "text/css";
      }
    }
    response.setContentType(contentType);
    PrintWriter writer = response.getWriter();
    if ("text/javascript".equals(contentType)) {
      writer.print(this.result.getJs().getContents());
    } else if ("text/css".equals(contentType)) {
      writer.print(this.result.getCss().getContents());
    }
    writer.close();
  }

}