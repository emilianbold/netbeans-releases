/*
 * Created on 14 Декабрь 2006 г., 13:58
 */

package org.server;

/**
 *
 * @author Danila_Dugurov
 */
public abstract class AbstractServer {
  
  protected  final int serverPort;
  protected  final String testDataPath;
  
  protected  AbstractServer(String testDataPath, int serverPort) {
    this.testDataPath = testDataPath;
    this.serverPort = serverPort;
  }
  
  public abstract void start() throws Exception;
  public abstract void stop() throws Exception;
}
