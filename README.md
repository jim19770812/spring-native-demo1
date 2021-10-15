用spring-native把springboot项目编译成原生应用

0.介绍
通过GraalVM实现的编译过程，所以要安装GraalVM和native-image

1.安装和配置
1.1.jvm的配置
要求jdk11+，GraalVM 21.0.0也可以

1.2.native-image安装

1.3.maven的要求
暂时不要使用maven阿里云源，因为阿里云上没有spring-native的编译插件

2.编译
spring-native目前只能编译成一个docker镜像，以docker run的方式执行程序

$ mvn spring-boot:build-image 

docker镜像 pack.local/builder/clpctrfrcn:latest
从github上下载graalvm，所以注意加梯子

....
[INFO] Successfully built image 'docker.io/library/spring-native-demo1:0.0.1-SNAPSHOT'
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  41:25 min
[INFO] Finished at: 2021-05-15T19:20:57+08:00
[INFO] ------------------------------------------------------------------------

编译时间较长，编译成功后会编译输出一个docker镜像，需要用docker run来执行
$ docker images -a | grep -i native
spring-native-demo1                 0.0.1-SNAPSHOT               ae2a23c4d724   41 years ago    61.3MB

启动镜像需要镜像名字:版本号

3.启动镜像
$ docker run --rm spring-native-demo1:0.0.1-SNAPSHOT
2021-05-15 11:31:38.862  INFO 1 --- [           main] o.s.nativex.NativeListener               : This application is bootstrapped with code generated with Spring AOT

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.5.5)

2021-05-15 11:31:38.863  INFO 1 --- [           main] c.e.demo1.SpringNativeDemo1Application   : Starting SpringNativeDemo1Application using Java 11.0.10 on 9fb436d7727a with PID 1 (/workspace/com.example.demo1.SpringNativeDemo1Application started by cnb in /workspace)
2021-05-15 11:31:38.863  INFO 1 --- [           main] c.e.demo1.SpringNativeDemo1Application   : No active profile set, falling back to default profiles: default
2021-05-15 11:31:38.871  INFO 1 --- [           main] c.e.demo1.SpringNativeDemo1Application   : Started SpringNativeDemo1Application in 0.015 seconds (JVM running for 0.017)
hello spring native

4.改成http服务
4.1.代码
@RestController
@RequestMapping(value="/v1")
public class HelloController {
    @RequestMapping("/hello")//curl http://localhost:8080/v1/hello
    public String index() {
        return "Hello World\n";
    }

    @RequestMapping("/echo/{text}")//curl http://localhost:8080/v1/echo/123123
    public String echo(@PathVariable("text") String text){
        return text+"\n";
    }
}

4.2.再次编译后启动
$ docker run --rm -p 8081:8081 spring-native-demo1:0.0.1-SNAPSHOT -t demo1:1

$ curl http://localhost:8081/v1/hello
$ hello world
$

5.分析
5.1.无法进入镜像

如果打包的镜像是

    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <image>
            <builder>paketobuildpacks/builder:tiny</builder>
            <env>
                <BP_NATIVE_IMAGE>true</BP_NATIVE_IMAGE>
            </env>
        </image>
    其中的paketobuildpacks/builder:tiny表示这是一个极简版本，不包含shell的，可以把tiny改成full再重新打包就能带shell了。

5.2.获取文件路径
$ docker inspect <容器ID> | more

其中的$.Config.Env.CNB_APP_DIR=/workspace 表示应用程序的工作空间,这里面有个执行文件 com.example.demo1.SpringNativeDemo1Application 就是程序启动的执行文件，可以把他拷贝出来直接执行

$ docker cp <容器ID>:/workspace/* .

不过最好还是在镜像里执行，一方面每次cp执行文件都很罗嗦，另外也失去了docker的一键部署的便利性


9.注意事项
9.1.使用idea里面的mvn package是不能编译成原生的
9.2.如果执行遇到OOM，需要把docker内存加大到8G
9.3.不是所有的springboot包都支持spring-native，在编译的时候会给出提示的
9.4.目前spring-native仍然不够稳定，暂时还不能用于生产
