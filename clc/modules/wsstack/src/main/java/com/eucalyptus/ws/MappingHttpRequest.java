package com.eucalyptus.ws;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.bouncycastle.util.encoders.UrlBase64;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.HttpHeaders;

public class MappingHttpRequest extends MappingHttpMessage implements HttpRequest {

  private final HttpMethod method;
  private final String     uri;
  private final String     servicePath;
  private String     query;
  private final Map<String,String> parameters;
  private String restNamespace;
  
  public MappingHttpRequest( HttpVersion httpVersion, HttpMethod method, String uri ) {
    super( httpVersion );
    this.method = method;
    this.uri = uri;
    try {
      URL url = new URL( "http://hi.com" + uri );
      this.servicePath = url.getPath( );
      this.parameters = new HashMap<String, String>( );
      this.query = url.toURI( ).getQuery( );
      this.populateParameters();
    } catch ( MalformedURLException e ) {
      throw new RuntimeException( e );
    } catch ( URISyntaxException e ) {
      throw new RuntimeException( e );
    }
  }

  private void populateParameters( ) {
    if ( this.query != null && !"".equals(  this.query ) ) {
      for ( String p : this.query.split( "&" ) ) {
        String[] splitParam = p.split( "=" );
        String lhs = splitParam[0];
        String rhs = splitParam.length == 2 ? splitParam[1] : null;
        this.parameters.put( lhs, rhs );
      }
    }
  }
  
  public MappingHttpRequest( final HttpVersion httpVersion, final HttpMethod method, final String host, final int port, final String servicePath, final Object source ) {
    super( httpVersion );
    this.method = method;
    this.uri = "http://" + host + ":" + port + servicePath;
    this.servicePath = servicePath;
    this.query = null;
    this.parameters = null;
    super.setMessage( source );
    this.addHeader( HttpHeaders.Names.HOST, host + ":" + port );
  }

  public String getServicePath( ) {
    return this.servicePath;
  }

  public String getQuery( ) {
    return this.query;
  }
  
  public void setQuery( String query ) {
    this.query = query;
    this.populateParameters( );
  }


  public HttpMethod getMethod( ) {
    return this.method;
  }

  public String getUri( ) {
    return this.uri;
  }

  @Override
  public String toString( ) {
    return this.getMethod( ).toString( ) + ' ' + this.getUri( ) + ' ' + super.getProtocolVersion( ).getText( );
  }

  public Map<String,String> getParameters( ) {
    return parameters;
  }

  public String getRestNamespace( ) {
    return restNamespace;
  }

  public void setRestNamespace( String restNamespace ) {
    this.restNamespace = restNamespace;
  }

  public String getAndRemoveHeader(String key) {
	  String value = getHeader(key);
	  removeHeader(key);
	  return value;
  }
}
