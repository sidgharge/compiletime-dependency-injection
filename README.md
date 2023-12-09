# compiletime-dependency-injection
Basic compile time dependency injection through annotation processing

## To Debug annotation processor in the project
To debug, run the below command in your project
> mvnDebug clean install

Then in intellij go to **Edit Configuration** > Click on **+** button > Click on **Remote JVM Debug** > Give a **name** to configuration and set the port to **8000**
Now a new run configuration is created which you can run on **debug mode**.

You should be able to debug the annotation processor code with debug points.