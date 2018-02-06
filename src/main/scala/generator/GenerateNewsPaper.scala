package generator

import com.typesafe.scalalogging.StrictLogging
import org.quartz.{Job, JobExecutionContext}

class GenerateNewsPaper extends Job with StrictLogging {
  def execute(context: JobExecutionContext): Unit = {
    //TODO parse CSV from yesterday and generate Feed
    logger.info("unimplemented")
  }
}
