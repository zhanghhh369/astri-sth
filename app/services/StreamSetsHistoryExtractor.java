package services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.typesafe.config.Config;

import akka.actor.*;
import play.inject.ApplicationLifecycle;
import scala.concurrent.duration.Duration;
import utils.EventLogger;

@Singleton
public class StreamSetsHistoryExtractor {

	private final ActorSystem actorSystem;
    private final OptThreadPool optThreadPool;
    public EventLogger eventLogger;
    
    
    private final int INITIAL_DELAY_VALUE;
    private final TimeUnit INITIAL_DELAY_UNIT;
    private final int INTERVAL_VALUE;
    private final TimeUnit INTERVAL_UNIT;
    public List<String> pipelineIds = new ArrayList<String>();
    private final Cancellable can;
    
    @Inject
    public StreamSetsHistoryExtractor(Config config, ActorSystem actorSystem, ApplicationLifecycle appLifecycle,
                                OptThreadPool optThreadPool, EventLogger eventLogger) {
        this.actorSystem = actorSystem;
        this.optThreadPool = optThreadPool;
        this.eventLogger = eventLogger;

        // Initialize configuration args
        INITIAL_DELAY_VALUE = config.hasPath("streamsets.scheduler.initialDelayValue") ? config.getInt("streamsets.scheduler.initialDelayValue") : 60;
        INTERVAL_VALUE = config.hasPath("streamsets.scheduler.intervalValue") ? config.getInt("streamsets.scheduler.intervalValue") : 1;
        String sInitialDelayValue = config.hasPath("streamsets.scheduler.initialDelayUnit") ? config.getString("streamsets.scheduler.initialDelayUnit").toUpperCase() : "SECONDS";
        String sIntervalUnit = config.hasPath("streamsets.scheduler.intervalUnit") ? config.getString("streamsets.scheduler.intervalUnit").toUpperCase() : "DAYS";
        INITIAL_DELAY_UNIT = TimeUnit.valueOf(sInitialDelayValue);
        INTERVAL_UNIT = TimeUnit.valueOf(sIntervalUnit);

        can = this.initialize();
        appLifecycle.addStopHook(() -> {
            cancelScheduler(can);
            return CompletableFuture.completedFuture(null);
        });
    }
    
    public void cancelScheduler() {
        can.cancel();
    }

    private Cancellable initialize() {
        return this.actorSystem.scheduler().schedule(
        		Duration.create(INITIAL_DELAY_VALUE, INITIAL_DELAY_UNIT),
                Duration.create(INTERVAL_VALUE, INTERVAL_UNIT),
                eventLogger::pullEventLogFromStreamSets,
                this.optThreadPool);
    }

    private void cancelScheduler(Cancellable cancellable) {
        cancellable.cancel();
    }
	
}
