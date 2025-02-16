package com.vidarin.wheatrevolution.util;

import com.vidarin.wheatrevolution.main.WheatRevolution;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mod.EventBusSubscriber(modid = WheatRevolution.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.DEDICATED_SERVER)
public class TickScheduler {
    private static final List<Task> TASKS = new ArrayList<>();

    public static void scheduleTask(Runnable task, int delay) {
        TASKS.add(new Task(task, delay));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        Iterator<Task> iterator = TASKS.iterator();
        while(iterator.hasNext()){
            Task scheduled = iterator.next();
            scheduled.delay--;
            if(scheduled.delay <= 0){
                scheduled.task.run();
                iterator.remove();
            }
        }
    }

    private static class Task {
        final Runnable task;
        int delay;

        private Task(Runnable task, int delay) {
            this.task = task;
            this.delay = delay;
        }
    }
}
