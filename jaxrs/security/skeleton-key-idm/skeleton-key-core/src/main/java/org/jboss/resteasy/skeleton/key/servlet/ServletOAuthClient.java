package org.jboss.resteasy.skeleton.key.servlet;

import org.jboss.resteasy.plugins.server.servlet.ServletUtil;
import org.jboss.resteasy.skeleton.key.AbstractOAuthClient;
import org.jboss.resteasy.skeleton.key.representations.AccessTokenResponse;
import org.jboss.resteasy.spi.ResteasyUriInfo;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ServletOAuthClient extends AbstractOAuthClient
{
   /**
    * Start the process of obtaining an access token by redirecting the browser
    * to the authentication server
    *
    *
    *
    * @param relativePath path relative to context root you want auth server to redirect back to
    * @param request
    * @param response
    * @throws IOException
    */
   public void redirectRelative(String relativePath, HttpServletRequest request, HttpServletResponse response) throws IOException
   {
      ResteasyUriInfo uriInfo = ServletUtil.extractUriInfo(request, null);
      String redirect = uriInfo.getBaseUriBuilder().path(relativePath).toTemplate();
      redirect(redirect, request, response);
   }


   /**
    * Start the process of obtaining an access token by redirecting the browser
    * to the authentication server
    *
    * @param redirectUri  full URI you want auth server to redirect back to
    * @param request
    * @param response
    * @throws IOException
    */
   public void redirect(String redirectUri, HttpServletRequest request, HttpServletResponse response) throws IOException
   {
      String state = getStateCode();

      URI url = UriBuilder.fromUri(authUrl)
              .queryParam("client_id", clientId)
              .queryParam("redirect_uri", redirectUri)
              .queryParam("state", state)
              .build();
      String cookiePath = request.getContextPath();
      if (cookiePath.equals("")) cookiePath = "/";

      Cookie cookie = new Cookie(stateCookieName, state);
      cookie.setSecure(true);
      cookie.setPath(cookiePath);
      response.addCookie(cookie);
      response.sendRedirect(url.toString());
   }

   protected String getCookieValue(String name, HttpServletRequest request)
   {
      if (request.getCookies() == null) return null;

      for (Cookie cookie : request.getCookies())
      {
         if (cookie.getName().equals(name)) return cookie.getValue();
      }
      return null;
   }

   /**
    * Obtain the code parameter from the url after being redirected back from the auth-server.  Then
    * do an authenticated request back to the auth-server to turn the access code into an access token.
    *
    * @param request
    * @return
    * @throws BadRequestException
    * @throws InternalServerErrorException
    */
   public String getBearerToken(HttpServletRequest request) throws BadRequestException, InternalServerErrorException
   {
      String stateCookie = getCookieValue(stateCookieName, request);
      if (stateCookie == null) throw new BadRequestException(new Exception("state cookie not set"));;

      String state = request.getParameter("state");
      if (state == null) throw new BadRequestException(new Exception("state parameter was null"));
      if (!state.equals(stateCookie))
      {
         throw new BadRequestException(new Exception("state parameter invalid"));
      }
      String code = request.getParameter("code");
      if (code == null) throw new BadRequestException(new Exception("code parameter was null"));
      return resolveBearerToken(code);
   }


}
