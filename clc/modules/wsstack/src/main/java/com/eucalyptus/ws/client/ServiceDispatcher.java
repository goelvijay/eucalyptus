package com.eucalyptus.ws.client;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.log4j.Logger;
import com.eucalyptus.component.Component;
import com.eucalyptus.component.Dispatcher;
import com.eucalyptus.component.ServiceConfiguration;
import com.eucalyptus.util.EucalyptusCloudException;
import com.eucalyptus.util.FullName;
import com.eucalyptus.util.LogUtil;
import com.eucalyptus.ws.client.pipeline.InternalClientPipeline;
import com.eucalyptus.ws.handlers.NioResponseHandler;
import com.google.common.collect.Lists;
import edu.ucsb.eucalyptus.msgs.BaseMessage;

public abstract class ServiceDispatcher implements Dispatcher {
  private static Logger LOG = Logger.getLogger( ServiceDispatcher.class );
  private static ConcurrentMap<FullName,Dispatcher> proxies = new ConcurrentHashMap<FullName,Dispatcher>(); 
  
  public static Dispatcher lookupSingle( Component c ) throws NoSuchElementException {
    List<Dispatcher> dispatcherList = lookupMany( c );
    if( dispatcherList.size( ) < 1 ) {
      LOG.error( "Failed to find service dispatcher for component=" + c );
      throw new NoSuchElementException("Failed to find service dispatcher for component=" + c);
    }
    return dispatcherList.get(0);
  }
  public static List<Dispatcher> lookupMany( Component c ) {
    List<Dispatcher> dispatcherList = Lists.newArrayList( );
    for( FullName key : proxies.keySet( ) ) {
      if( c.getComponentId( ).getName( ).equals( key.getNamespace( ) ) ) {
        dispatcherList.add( proxies.get( key ) );
      }
    }
    return dispatcherList;
  }
  public static Dispatcher lookup( ServiceConfiguration config ) {
    return proxies.get( config.getFullName( ) );
  }

  public static Dispatcher register( ServiceConfiguration serviceConfiguration, Dispatcher proxy ) {
    LOG.info( "Registering "+ serviceConfiguration.getFullName( ).toString( ) + " as "  + proxy );
    return proxies.put( serviceConfiguration.getFullName( ), proxy );
  }
  public static Dispatcher deregister( ServiceConfiguration serviceConfiguration ) {
    LOG.info( "Deregistering "+ serviceConfiguration.getFullName( ).toString( ) );
    return proxies.remove( serviceConfiguration.getFullName( ) );
  }
  public static Collection<Dispatcher> values( ) {
    return proxies.values( );
  }

  private Component     component;
  private String        name;
  private URI           address;
  private boolean isLocal = false;

  public ServiceDispatcher( Component component, String name, URI address, boolean isLocal ) {
    super( );
    this.component = component;
    this.name = name;
    this.address = address;
    this.isLocal = isLocal;
  }

  /**
   * @see com.eucalyptus.component.Dispatcher#dispatch(edu.ucsb.eucalyptus.msgs.BaseMessage)
   * @param msg
   */
  public abstract void dispatch( BaseMessage msg );

  /**
   * @see com.eucalyptus.component.Dispatcher#send(edu.ucsb.eucalyptus.msgs.BaseMessage)
   * @param <REPLY>
   * @param message
   * @param replyType
   * @return
   * @throws EucalyptusCloudException
   */
  @SuppressWarnings( "unchecked" )
  public abstract <REPLY extends BaseMessage> REPLY send( BaseMessage message ) throws EucalyptusCloudException;
  
  /**
   * @see com.eucalyptus.component.Dispatcher#getComponent()
   * @return
   */
  public Component getComponent( ) {
    return component;
  }
  /**
   * @see com.eucalyptus.component.Dispatcher#getName()
   * @return
   */
  public String getName( ) {
    return name;
  }
  /**
   * @see com.eucalyptus.component.Dispatcher#getAddress()
   * @return
   */
  public URI getAddress( ) {
    return address;
  }
  /**
   * @see com.eucalyptus.component.Dispatcher#isLocal()
   * @return
   */
  public boolean isLocal( ) {
    return isLocal;
  }

  protected NioClient getNioClient( ) throws Exception {
    return new NioClient( this.address.getHost( ), this.address.getPort( ), this.address.getPath( ), new InternalClientPipeline( new NioResponseHandler( ) ) );
  }
  /**
   * @see com.eucalyptus.component.Dispatcher#toString()
   * @return
   */
  @Override
  public String toString( ) {
    return LogUtil.dumpObject( this );
  }

  
  
}