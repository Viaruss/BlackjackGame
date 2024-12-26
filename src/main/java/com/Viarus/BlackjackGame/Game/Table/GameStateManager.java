package com.Viarus.BlackjackGame.Game.Table;

import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class GameStateManager {
    private final TaskScheduler taskScheduler;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private ScheduledFuture<?> scheduledTask;
    final private TableService tableService;

    public GameStateManager(TaskScheduler taskScheduler,
                            SimpMessagingTemplate simpMessagingTemplate,
                            @Lazy TableService tableService
    ) {
        this.taskScheduler = taskScheduler;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.tableService = tableService;
    }

    public synchronized void setState(String tableId, GameState newState) {
        System.out.println("SCHEDULER - Setting state to " + newState);
        tableService.changeGameState(tableId, newState);
        notifyClients(tableId);
    }

    public void notifyClients(String tableId) {
        Table table = tableService.getTable(tableId);
        simpMessagingTemplate.convertAndSend("/topic/table/" + tableId, table);
    }

    public synchronized void scheduleStateChange(String tableId, GameState newState, long delayInSeconds) {
        cancelScheduledTask();
        System.out.printf("SCHEDULER - Scheduling state change to %s in %d seconds%n", newState, delayInSeconds);
        scheduledTask = taskScheduler.schedule(() -> setState(tableId, newState), new java.util.Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(delayInSeconds)));
    }

    public synchronized void cancelScheduledTask() {
        if (scheduledTask != null) {
            System.out.println("SCHEDULER - Canceling previously scheduled task");
            scheduledTask.cancel(false);
            scheduledTask = null;
        }
    }
}
