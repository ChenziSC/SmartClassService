package controller;

import domain.QuartzManager;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import service.SurroundingsService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
@Component
@RequestMapping(value = "/QuartzManagerSurroundings")
public class QuartzManagerSurroundings {

    @Autowired
    QuartzManager quartzManager;

    @Autowired
    private SurroundingsService surroundingsService;

    public void addSurroudings() {
        System.out.println("addSurroudings");
        surroundingsService.addSurroudings();
    }

    @RequestMapping(value = "/addSurroundings", method = RequestMethod.POST)
    @ResponseBody
    public void addQuartzManager() {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//        String nowDate = dateFormat.format(new Date());
        try {
            List<String> jobGroupNames = quartzManager.scheduler.getJobGroupNames();
            boolean exist = false;
            for (String groupNames : jobGroupNames) {
                if (groupNames.equals("surroudings")) {
                    for (JobKey jobKey : quartzManager.scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupNames))) {
                        String jobName = jobKey.getName();
                        String jobGroup = jobKey.getGroup();
                        //get job's trigger
                        if (jobName.equals("addSurroudings")) {
                            exist = true;
                            break;
                        }
                    }
                }
            }

            if (!exist) {
                String corn = "*/10 * * * * ?";
                quartzManager.addJob(
                        "addSurroudings",
                        "surroudings",
                        "addSurroudings",
                        "surroudings",
                        SurroundingJob.class,
                        corn);
            }

        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/removeAllJob", method = RequestMethod.POST)
    @ResponseBody
    public void removeAllJob() {
        quartzManager.removeAllJob("surroudings", "surroudings");
    }

    @RequestMapping(value = "/removeJob", method = RequestMethod.POST)
    @ResponseBody
    public void removeJob(@RequestBody String jobName) {
        quartzManager.removeJob(jobName, "surroudings", jobName, "surroudings");
    }
}
