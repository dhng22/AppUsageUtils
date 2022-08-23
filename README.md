# AppUsageUtils
Query and return application usage time by day or week
## How to use
### Query for applications' time usage
**AppUsage.queryUsageTime(context, isDaily)**

   <sub>context: the context</sub>
   
   <sub>isDaily: true for today result, false for this week result</sub>


### Return value
**class AppUsedTime{

  ...
  
}**

<sub>packageName: the package name associated with this object</sub>

<sub>totalUsedTime: the time that this application has used</sub>

<sub>timeStampStart: the time stamp of this evaluation</sub>

<sub>isDaily: is this evaluation daily or weekly. true for daily and otherwise</sub>


 

