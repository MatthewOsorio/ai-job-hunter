// package com.jobhunter.job;

// import com.jobhunter.ai.ClaudeService;
// import com.jobhunter.ai.FilterResult;
// // import com.jobhunter.profile.ProfileBuilder;

// import java.util.List;
// import java.util.stream.Collectors;

// public class JobFilter {
// private final ClaudeService claudeService = new ClaudeService();
// // private final String profile = new ProfileBuilder().build();

// /** Filters a list of jobs, returning only those Claude thinks are a good match. */
// public List<Job> filter(List<Job> jobs) {
// return jobs.stream()
// .peek(this::filterOne)
// .filter(Job::isShouldApply)
// .collect(Collectors.toList());
// }

// /** Runs filter on a single job, mutating it with the result. */
// public void filterOne(Job job) {
// FilterResult result = claudeService.filterJob(profile, job.getDescription());
// job.setShouldApply(result.shouldApply());
// job.setMatchScore(result.matchScore());
// job.setFilterReason(result.reason());

// System.out.printf("Filter [%d/100] %s at %s — %s%n",
// result.matchScore(), job.getTitle(), job.getCompany(), result.reason());
// }
// }
