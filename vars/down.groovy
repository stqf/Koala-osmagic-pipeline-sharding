import com.osmagic.pipeline.sharding.utils.CommUtils

def clone(Map project) {
    String nameItem = project.get("project")
    def addressKey = "$nameItem-address".replaceAll("-", "_")
    def address = env."$addressKey"
    if (!address) {
        error "Project[$nameItem] address is unknown ... $addressKey "
    }

    def branchItem = params.get("Koala-osmaigc-all-barnch")

    sh """
        if [ -d "$nameItem" ]; then
            cd $nameItem && git reset --hard HEAD && git checkout $branchItem && git pull 
            echo "[$nameItem]更新代码 ..."
        else
            git clone $address -b $branchItem
            echo "[$nameItem]拉取代码 ..."
        fi
    """
}

def clones(List projects) {
    def tasks = [:]
    projects.each {
        def nameItem = it.get("project")

        def requireItem = CommUtils.isRequireHandler(nameItem, params)
        println("Clone $nameItem ... requireItem: $requireItem ")

        if (!requireItem) {
            return
        }

        tasks."Project[$nameItem]" = {
            clone(it)
            echo "Project[$nameItem] Clone finish ..."
        }
    }

    parallel tasks
}