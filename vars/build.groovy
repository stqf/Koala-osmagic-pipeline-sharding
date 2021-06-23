import com.osmagic.pipeline.sharding.utils.CommUtils

def build(String typeItem, String project, String kItem, String currentTag, Map item) {
    def tarItem
    def regexItem = item.get("regexItem")
    if ("Java".equals(typeItem)) {
        tarItem = sh(script: "find ./ -name $regexItem | sort -r | head -n 1", returnStdout: true).trim()
    } else if ("Web".equals(typeItem)) {
        tarItem = sh(script: "find ./$project -name $regexItem | tail -n 1", returnStdout: true).trim()
    } else {
        tarItem = ""
        error "不能支持的类型[$typeItem], 请联系管理员 ... 项目:$project"
    }
    def dockerfileItem = item.get("dockerfile")
    String dockerfileMatchesItem = dockerfileItem == null ? "Dockerfile" : dockerfileItem
    String imageName = item.get("image")
    String imageItem = "$imageName:$currentTag"
    String workspaceItem = "Builds/$kItem"
    String jarItem = sh(script: "basename $tarItem", returnStdout: true).trim()
    String podWsItem = sh(script: "find ./devops -name '$typeItem*'", returnStdout: true).trim()

    /*构建镜像*/
    sh """
        mkdir -pv $workspaceItem
        \\cp -avr $tarItem $workspaceItem
        cat ${env.WORKSPACE}/$podWsItem/start.sh > $workspaceItem/start.sh
        cat ${env.WORKSPACE}/$podWsItem/$dockerfileMatchesItem > $workspaceItem/Dockerfile
        sed "s/JAVA_NAME/$jarItem/g" -i $workspaceItem/Dockerfile
        cd $workspaceItem
        docker build -t $imageItem ./
        cd ${env.WORKSPACE};pwd
    """

    /*推送和移除镜像*/
    sh """
        docker login -u admin -p Harbor12345 hub.kaolayouran.cn:5000
        docker tag $imageItem $imageName:latest
        docker push $imageItem
        docker push $imageName:latest
        docker rmi $imageItem
    """

    println("[DEBUG] build modolus finish: name is $project, modolus is $kItem, tarItem：$tarItem, image is $imageItem ")
}

def builds(List projects, String currentTag) {
    def tasks = [:]
    projects.each {

        def nameItem = it.get("project")
        String typeItem = it.get("type")

        def requireItem = CommUtils.isRequireHandler(nameItem, params)
        println("[TRACE] Build project start : name is $nameItem, require is $requireItem ")

        /*判断项目是否需要Build相应镜像*/
        if (!requireItem) {
            return
        }

        List<Map> deployItems = it.get("deploy")

        for (Map item : deployItems) {
            def suffixItem = item.get("name")
            def kItem = suffixItem == null ? nameItem : "$nameItem-$suffixItem"
            def requireIt = CommUtils.isRequireHandler(kItem, params)
            /*判断项目下某一模块是否需要构建镜像, 主要用于Java多模块项目*/
            if (!requireIt) {
                return
            }

            def kItemSwap = kItem
            def itemSwap = new HashMap(item)
            tasks."Project[$kItemSwap]" = {
                /*调用Docker镜像构建方法, 传参依次是:项目类型、项目名称、模块名称、镜像TAG、模块描述信息*/
                build(typeItem, nameItem, kItemSwap, currentTag, itemSwap)
                println("[INFO ] Build project finish: name is $nameItem, modolus is $kItemSwap ")
            }
        }

    }

    parallel tasks
}