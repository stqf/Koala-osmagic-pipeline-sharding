# Koala-osmagic-pipeline-sharding

Jenkins[Pipeline]共享库



## 项目的描述性对象

```groovy
def pods = [
    [
        type: "Java",
        project: "Koala-osmagic-all",
        deploy: [
            {
                regexItem: "*eureka*.jar",
                image: "www.osmagic.com:5000/osmagic-all/java-01-micro-eureka:0001",
                resources: ["java-01-micro-eureka-01", "java-01-micro-eureka-02", "java-01-micro-eureka-03"]  
            }
        ]
    ], 
    [
        type: "Web",
        project: "Koala-osmagic-learn-power-web",
        deploy: [
            {
                regexItem: "*dist",
                image: "www.osmagic.com:5000/osmagic-all/web-21-learn-power:0001",
                resources: ["web-21-learn-power-web"]  
            }
        ]
    ]
]
```

