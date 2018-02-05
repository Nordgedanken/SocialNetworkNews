package generator

import com.typesafe.config.{Config, ConfigFactory}
import org.quartz.CronScheduleBuilder.cronSchedule
import org.quartz.{CronTrigger, SchedulerException}
import org.quartz.JobBuilder.newJob
import org.quartz.TriggerBuilder.newTrigger
import org.quartz.impl.StdSchedulerFactory


object Scheduler {
  val conf: Config = ConfigFactory.load()

  def main(): Unit = {
    val cronString = conf.getString("generator.cronString")
    try { // Grab the Scheduler instance from the Factory
      val sf = new StdSchedulerFactory
      val scheduler = sf.getScheduler
      // and start it off
      scheduler.start()

      val job = newJob(classOf[GenerateNewsPaper]).withIdentity("generateNewsPaper", "generator").build

      // Trigger the job to run now, and then repeat every 40 seconds
      val trigger: CronTrigger = newTrigger.withIdentity("daily", "generator").withSchedule(cronSchedule(cronString)).build

      // Tell quartz to schedule the job using our trigger
      scheduler.scheduleJob(job, trigger)

      scheduler.shutdown()
    } catch {
      case se: SchedulerException =>
        se.printStackTrace()
    }
  }

}
