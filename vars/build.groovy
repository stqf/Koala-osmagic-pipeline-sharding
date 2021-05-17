import com.osmagic.pipeline.sharding.utils.CommUtils

def build(String typeItem, String project, String kItem, String currentTag, Map item) {
    def tarItem
    def regexItem = item.get("regexItem")
    if ("Java".equals(typeItem)) {
        tarItem = sh(script: "find ./ -name $regexItem | sort -r | head -n 1", returnStdout: true).trim()
    } else if ("Web".equals(typeItem)) {
        tarItem = sh(script: "find ./ -name $regexItem | sort | head -n 1", returnStdout: true).trim()
    } else {
        tarItem = ""
        error "不能支持的类型[$typeItem], 请联系管理员 ... 项目:$project"
    }
    String imageName = item.get("image")
    String imageItem = "$imageName:$currentTag"
    String workspaceItem = "Builds/$kItem"
    String jarItem = sh(script: "basename $tarItem", returnStdout: true).trim()

    // TODO 构建镜像
    sh """
        mkdir -pv $workspaceItem
        \\cp -avr $tarItem $workspaceItem
        cat ${env.WORKSPACE}/devops/$typeItem\\_Dockerfie/start.sh > $workspaceItem/start.sh
        cat ${env.WORKSPACE}/devops/$typeItem\\_Dockerfie/Dockerfile > $workspaceItem/Dockerfile
        sed "s/JAVA_NAME/$jarItem/g" -i $workspaceItem/Dockerfile
        cd $workspaceItem
        docker build -t $imageItem ./
        cd ${env.WORKSPACE}};pwd
    """

    // TODO 推送和移除镜像
    sh """
        docker login -u admin -p Harbor12345 hub.kaolayouran.cn:5000
        docker tag $imageItem hub.kaolayouran.cn:5000/osmagic-all/$imageName:latest
        docker push $imageItem
        docker push hub.kaolayouran.cn:5000/osmagic-all/$imageName:latest
        docker rmi $imageItem
    """

    println("Build[$project] finish ... tarItem：$tarItem")
}

def builds(List projects, String currentTag) {
    def tasks = [:]
    projects.each {

        def nameItem = it.get("project")
        String typeItem = it.get("type")

        def requireItem = CommUtils.isRequireHandler(nameItem, params)
        println("Build $nameItem ... requireItem: $requireItem ")

        if (!requireItem) {
            return
        }

        List<Map> deployItems = it.get("deploy")

        for (Map item : deployItems) {
            def suffixItem = item.get("name")
            def kItem = suffixItem == null ? nameItem : "$nameItem-$suffixItem"
            def requireIt = CommUtils.isRequireHandler(kItem, params)
            if (!requireIt) {
                return
            }

            tasks."Project[$nameItem]" = {
                build(typeItem, nameItem, kItem, currentTag, item)
                echo "Project[$nameItem] Build finish ..."
            }
        }

    }

    parallel tasks
}