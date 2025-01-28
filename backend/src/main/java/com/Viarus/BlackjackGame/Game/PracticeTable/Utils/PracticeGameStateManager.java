package com.Viarus.BlackjackGame.Game.PracticeTable.Utils;

import com.Viarus.BlackjackGame.Game.PracticeTable.PracticeTable;
import com.Viarus.BlackjackGame.Game.PracticeTable.PracticeTableService;
import com.Viarus.BlackjackGame.Game.Table.Utils.GameState;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class PracticeGameStateManager {
    private final TaskScheduler taskScheduler;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private ScheduledFuture<?> scheduledTask;
    final private PracticeTableService practiceTableService;

    public PracticeGameStateManager(TaskScheduler taskScheduler,
                                    SimpMessagingTemplate simpMessagingTemplate,
                                    @Lazy PracticeTableService practiceTableService
    ) {
        this.taskScheduler = taskScheduler;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.practiceTableService = practiceTableService;
    }

    public synchronized void setState(String tableId, GameState newState)  {
        try {
            practiceTableService.changeGameState(tableId, newState);
            notifyClients(tableId);
        } catch (Exception e) {
            System.out.println("SCHEDULER - Error setting state: " + e.getMessage());
        }
    }

    public void notifyClients(String tableId) {
        PracticeTable table = practiceTableService.getTable(tableId);
        simpMessagingTemplate.convertAndSend("/topic/practiceTable/" + tableId, table);
    }

    public synchronized void scheduleStateChange(String tableId, GameState newState, long delayInSeconds) {
        cancelScheduledTask();
        scheduledTask = taskScheduler.schedule(() -> setState(tableId, newState), new java.util.Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(delayInSeconds)));
    }

    public synchronized void cancelScheduledTask() {
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            scheduledTask = null;
        }
    }
}
