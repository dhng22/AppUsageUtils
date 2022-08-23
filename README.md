# AppUsageUtils
Query and return application usage time by day or week
## How to use
### Query for applications' time usage
AppUsage.queryUsageTime(context, isDaily)

   context: the context
   
   isDaily: true for today result, false for this week result


### Return value
class AppUsedTime{

  ...
  
}

packageName: the package name associated with this object

totalUsedTime: the time that this application has used

timeStampStart: the time stamp of this evaluation

isDaily: is this evaluation daily or weekly. true for daily and otherwise


 

