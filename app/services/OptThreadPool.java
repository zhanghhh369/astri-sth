/**
 * 
 */
package services;

import javax.inject.Inject;
import javax.inject.Singleton;

import akka.actor.ActorSystem;
import play.libs.concurrent.CustomExecutionContext;

/**
 * @author srwu
 *
 */
@Singleton
public class OptThreadPool extends CustomExecutionContext {

  @Inject
  public OptThreadPool(ActorSystem actorSystem, String name) {
    // uses a custom thread pool defined in application.conf
    super(actorSystem, "opt-pool");
  }


}
