/**
 *
 *  @author Koncki Igor S16692
 *
 */

package zad1;

import java.util.concurrent.TimeUnit;

public class Main {

  public static void main(String[] args) throws Exception {
	  runServerInNewJVM();
	  TimeUnit.SECONDS.sleep(1);
	  runClientInNewJVM();
	  runClientInNewJVM();
  }
  
  public static void runServerInNewJVM() throws Exception{
	    String path = System.getProperty("java.home") + System.getProperty("file.separator") + "bin" + System.getProperty("file.separator") + "java";
	    ProcessBuilder processBuilder = new ProcessBuilder(path, "-Dfile.encoding=utf-8", "-cp", System.getProperty("java.class.path"), Server.class.getCanonicalName());
	    processBuilder.start();
  }
  public static void runClientInNewJVM() throws Exception{
	    String path = System.getProperty("java.home") + System.getProperty("file.separator") + "bin" + System.getProperty("file.separator") + "java";
	    ProcessBuilder processBuilder = new ProcessBuilder(path, "-Dfile.encoding=utf-8", "-cp", System.getProperty("java.class.path"), Client.class.getCanonicalName());
	    processBuilder.start();
 }
}
