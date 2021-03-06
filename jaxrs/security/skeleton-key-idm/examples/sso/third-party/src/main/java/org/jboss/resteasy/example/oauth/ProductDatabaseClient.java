package org.jboss.resteasy.example.oauth;

import org.jboss.resteasy.client.jaxrs.AbstractClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.plugins.server.servlet.ServletUtil;
import org.jboss.resteasy.skeleton.key.SkeletonKeySession;
import org.jboss.resteasy.skeleton.key.servlet.ServletOAuthClient;
import org.jboss.resteasy.spi.ResteasyUriInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ProductDatabaseClient
{
   public static void redirect(HttpServletRequest request, HttpServletResponse response)
   {
      // This is really the worst code ever. The ServletOAuthClient is obtained by getting a context attribute
      // that is set in the Bootstrap context listenr in this project.
      // You really should come up with a better way to initialize
      // and obtain the ServletOAuthClient.  I actually suggest downloading the ServletOAuthClient code
      // and take a look how it works.
      ServletOAuthClient oAuthClient = (ServletOAuthClient)request.getServletContext().getAttribute(ServletOAuthClient.class.getName());
      try
      {
         oAuthClient.redirectRelative("pull_data.jsp", request, response);
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   public static List<String> getProducts(HttpServletRequest request)
   {
      // This is really the worst code ever. The ServletOAuthClient is obtained by getting a context attribute
      // that is set in the Bootstrap context listenr in this project.
      // You really should come up with a better way to initialize
      // and obtain the ServletOAuthClient.  I actually suggest downloading the ServletOAuthClient code
      // and take a look how it works.
      ServletOAuthClient oAuthClient = (ServletOAuthClient)request.getServletContext().getAttribute(ServletOAuthClient.class.getName());
      String token = oAuthClient.getBearerToken(request);
      ResteasyClient client = new ResteasyClientBuilder()
                 .truststore(oAuthClient.getTruststore())
                 .hostnameVerification(AbstractClientBuilder.HostnameVerificationPolicy.ANY).build();
      try
      {
         Response response = client.target("https://localhost:8443/database/products").request()
                 .header(HttpHeaders.AUTHORIZATION, "Bearer " + token).get();
         return response.readEntity(new GenericType<List<String>>(){});
      }
      finally
      {
         client.close();
      }
   }
}
