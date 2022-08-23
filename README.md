# AppUsageUtils
Query and return application usage time by day or week
## How to use
/**
* @param context: the context
* @param isDaily: true for today result, false for this week result
* @return list: of AppUsedTime
*/

AppUsage.queryUsageTime(context, isDaily)

/**
 * @property packageName: the package name associated with this object
 * @property totalUsedTime: the time that this application has used
 * @property timeStampStart: the time stamp of this evaluation
 * @property isDaily: is this evaluation daily or weekly. true for daily and otherwise
 */
 
class AppUsedTime{
  ...
}
