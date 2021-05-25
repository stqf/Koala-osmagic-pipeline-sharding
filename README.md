# Koala-osmagic-pipeline-sharding

Jenkins[Pipeline]共享库



## 使用示例

```groovy
@Library("Koala-osmagic-pipeline-sharding") _

pipeline {
    agent any

    parameters {
        choice(name: 'Koala-osmaigc-all-barnch', choices: ['dev', 'test', 'master'], description: 'Koala-osmagic-all分支')

        string(name: 'ServerIp', defaultValue: 'x.x.x.x', description: ' 服务器地址')
        password(name: 'ServerPs', defaultValue: '123456', description: ' 服务器root账号密码')

        booleanParam(name: '000-build-all-application', defaultValue: false, description: '构建全部应用')
        booleanParam(name: 'A01-Koala-osmagic-all-eureka', defaultValue: false, description: 'Java-Eureka注册中心')
        booleanParam(name: 'B01-Koala-osmagic-learn-power-web', defaultValue: false, description: 'AI演示平台')
    }

    environment {
        // 凭证ID(仅UsernamePassword类型)
        Sauce_Access = credentials('凭证ID') 
        // 需要通知钉钉标识ID, 需要Jenkins安装插件支持
        Koala_osmagic_ding_address = "xxxxxxxx-xxx-xxx-xxx-xxxxxxxx"
        // 需要认证项目地址示例, 地址变量命名格式必须为:项目名_address
        Koala_osmagic_all_address = "http://${Sauce_Access}@192.168.2.xxx/ns/Java/quick.git"
        // 不需要认证项目地址示例, 地址变量命名格式必须为:项目名_address
        Koala_osmagic_learn_power_web_address = "http://192.168.2.xxx/ns/Web/quick.git" 
    }

    stages {
        stage('Init') { // 此阶段不能少用于一些必要的初始化操作
            steps{
                script {
                    def strItem = new Date().format("yyyyHHddHHmmss")
                    // 每次构建镜像后的tag
                    currentTag = "$strItem-${env.BUILD_ID}"
                    // 整个构建流水线的描述数据
                    deploies = [
                        [
                            type: "Java", // 项目类型, 如Java、Web
                            project: "Koala-osmagic-all", // 项目名称
                            deploy: [
                                [
                                    name: "eureka",
                                    regexItem: "*eureka*.jar", 
                                    image: "hub.kaolayouran.cn:5000/osmagic-all/java-01-micro-eureka", // 镜像名称
                                    resources: ["java-01-micro-eureka-01", "java-01-micro-eureka-02", "java-01-micro-eureka-03"] 
                                ]
                            ]
                        ], 
                        [
                            type: "Web",
                            project: "Koala-osmagic-learn-power-web",
                            deploy: [
                                [
                                    regexItem: "*dist",
                                    image: "hub.kaolayouran.cn:5000/osmagic-all/web-21-learn-power",
                                    resources: ["web-21-learn-power-web"]  // 需要更新的POD列表
                                ]
                            ]
                        ]
                    ]
                }
				
                // 拉取Devops项目，用于执行自定义Dockerfile构建镜像或者打整包, 详见Devops说明
                sh """
                    if [ -d "devops" ]; then
                        cd devops && git reset --hard HEAD && git checkout master && git pull 
                        echo "更新代码 ..."
                    else
                        git clone http://${Sauce_Access}@192.168.2.xxx/ns/Devops/Dockerfile.git -b master devops
                        echo "拉取代码 ..."
                    fi
                """
            }
        }

        stage('Clone') {
            steps {
                script {
                    down.clones deploies
                }
                echo "Clone finish ..."
            }
        }

        stage('Compile') {
            steps {
                script {
                    compile.compiles deploies
                }
                echo "Compile finish ..."
            }
        }

        stage('Build') {
            steps {
                script {
                    build.builds deploies, currentTag
                }
                echo "Build finish ..."
            }
        }

        stage('Deploy') {
            steps {
                script {
                    deploy.deploys deploies, currentTag
                }
                echo "Deploy finish ..."
            }
        }
        
        // 打整包时执行,视具体情况斟酌使用
        stage('Package') {
            steps {
                script {
                    pack.call "产品(项目)名称"
                }
                echo "Package finish ..."
            }
        }

    }
    
    post {
        success {
            script {
                echo "流水线完成 ... "
                def commitItem = [:]
                for(Map deploy: deploies) {
                    def project = deploy.get("project")
                    def logs = sh(script: """ cd $project && git log -3 --pretty=format:"%h - [%ad]: %s" --date=format-local:"%m-%d %H:%M:%S" """, returnStdout: true).trim().split("\n")
                    commitItem."$project" = logs
                }

                def sbItem = new StringBuilder()
                def branch = params.get("Koala-osmaigc-all-barnch")
                commitItem.each {
                    def kItem = it.key
                    sbItem.append("**$kItem($branch)**提交记录:   \r\n")
                    def vItems = it.value
                    for (String vItem : vItems) {
                        def item;
                        if (vItem.length() > 39) {
                            item = vItem.substring(0, 40)
                        } else {
                            item = vItem;
                        }
                        sbItem.append("$item   \r\n")
                    }
                }
                // 上面一系列的操作都是为了获取所有项目最近三次的提交记录, 然后赋值给全局变量ctxItem
                ctxItem = sbItem.toString()
            }
            
            // 参考文档: https://jenkinsci.github.io/dingtalk-plugin/
            dingtalk(
                    atAll: true,
                    type: "MARKDOWN",
                    title: "你有新的Jenkins消息, 请注意查收",
                    robot: "${env.Koala_osmagic_ding_address}",
                    at: ['牛魔王', '黄狮精', '白骨精', '蜘蛛精', '齐天大圣'],
                    text: [
                            "# 某某项目终极版本",
                            "",
                            "#### 本次构建已经顺利完成, 具体内容详情如下:",
                            "---",
                            "${ctxItem}",
                            "---",
                            "![SCM](https://ss0.baidu.com/7Po3dSag_xI4khGko9WTAnF6hhy/baike/pic/item/203fb80e7bec54e7a9f8f039bb389b504fc26a85.jpg)", 
                            "---",
                            "- 执行人员:  无名氏",
                            "- 触发类型:  滚动更新",
                            "- 持续时间:  瞬间完成",
                            "- 打包类型:  我不告诉您",
                            "- 构建编号:  8808208820",
                            "- 任务名称:  一个伟大的梦想",
                            "- 目标地址:  **${params.ServerIp}**",
                            "- 构建结果:  构建成功✅✅✅✅✅✅",
                            "- 构建日志:  [点击查看详情](http://www.taobao.com)",
                            "---",
                            "[【更改记录】](http://www.baidu.com) &emsp;&emsp;&emsp; [【Jenkins控制台】](http://www.qq.com)",
                            "---",
                            "-------------------我也是有底线的--------------------"
                    ]
            )
        }
        failure {
            script {
                echo "流水线失败 ... "
            }
        }
        aborted {
            script {
                echo "流水线中断 ... "
            }
        }
    }

}

```

## Devops项目示例

### 目录结构示例

```
devops/
├── JavaDockerfile      --- Java项目
│   ├── Dockerfile      --- Dockerfile
│   └── start.sh        --- 容器启动执行命令
├── Package             --- 打整包
│   └── main.sh         --- 打整包入口程序
└── WebDockerfile       --- Web项目
    ├── Dockerfile      --- Dockerfile
    └── start.sh        --- 容器启动执行命令
```

### Dockerfile示例

```dockerfile
FROM hub.kaolayouran.cn:5000/osmagic-all/jdk:latest
WORKDIR /home/java
COPY JAVA_NAME . #JAVA_HOME这个是固定占位符,用于实际执行时候替换目标文件
COPY start.sh .
RUN chmod +x start.sh && echo "BUild Time------->>$(date)<<----------" > /root/BUild.log
```

