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
        string(name: 'ServerPs', defaultValue: '123456', description: ' 服务器密码')

        booleanParam(name: '000-build-all-application', defaultValue: false, description: '构建全部应用')
        booleanParam(name: 'A01-Koala-osmagic-all-eureka', defaultValue: false, description: 'Java-Eureka注册中心')
        booleanParam(name: 'B01-Koala-osmagic-learn-power-web', defaultValue: false, description: 'AI演示平台')
    }

    environment {
        Koala_osmagic_all_address = "http://192.168.2.xxx/ns/Java/quick.git"  // 项目地址, 格式必须为:项目名_address
        Koala_osmagic_learn_power_web_address = "http://192.168.2.xxx/ns/Web/quick.git"
    }

    stages {
        stage('Init') { // 此阶段不能少用于一些必要的初始化操作
            steps{
                script {
                    def strItem = new Date().format("yyyyHHddHHmmss")
                    currentTag = "$strItem-${env.BUILD_ID}" // 每次构建镜像后的tag
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
				
                // 拉取Devops下的Dockerfile用于构建镜像
                sh '''
                    if [ -d "devops" ]; then
                        cd devops && git reset --hard HEAD && git checkout master && git pull 
                        echo "更新代码 ..."
                    else
                        git clone http://192.168.2.xxx/ns/Devops/Dockerfile.git -b master devops
                        echo "拉取代码 ..."
                    fi
                '''
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

    }

}

```

