package com.jobhunter.cli;

import com.jobhunter.job.JobRunner;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import picocli.CommandLine.Command;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

@Command(name = "start", description = "Start the job hunter, running every hour until stopped.")
public class StartCommand implements Runnable {

    @Override
    public void run() {
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

            JobDetail job = newJob(PipelineJob.class)
                    .withIdentity("pipeline", "jobhunter")
                    .build();

            Trigger trigger = newTrigger()
                    .withIdentity("pipeline-trigger", "jobhunter")
                    .startNow()
                    .withSchedule(simpleSchedule()
                            .withIntervalInHours(1)
                            .repeatForever())
                    .build();

            scheduler.scheduleJob(job, trigger);
            scheduler.start();

            System.out.println("Job hunter started. Running every hour. Press Ctrl+C to stop.");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    System.out.println("Shutting down scheduler...");
                    scheduler.shutdown(true); // wait for running jobs to finish
                } catch (SchedulerException e) {
                    System.err.println("Error shutting down scheduler: " + e.getMessage());
                }
            }));

            Thread.currentThread().join(); // Block until Ctrl+C

        } catch (SchedulerException | InterruptedException e) {
            System.err.println("Scheduler error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static class PipelineJob implements Job {
        @Override
        public void execute(JobExecutionContext context) {
            System.out.println("Running pipeline at " + java.time.ZonedDateTime.now());
            try {
                new JobRunner().runAll();
            } catch (Exception e) {
                System.err.println("Pipeline error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
