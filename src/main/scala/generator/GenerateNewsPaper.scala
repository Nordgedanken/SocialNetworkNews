package generator

import org.quartz.{Job, JobExecutionContext}

class GenerateNewsPaper extends Job {
  override def execute(context: JobExecutionContext): Unit = {
    //TODO parse CSV from yesterday and generate Feed
    println("unimplemented")
  }
}
