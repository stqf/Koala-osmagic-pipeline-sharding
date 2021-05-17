package com.osmagic.pipeline.sharding.utils

class CommUtils {

    /**
     * 是否需要处理
     * @param name
     * @param params
     * @return
     */
    static boolean isRequireHandler(String nameItem, Map params) {

        def requireItem = params.get("000-build-all-application")
        if (!requireItem) {
            def kItems = params.keySet()
            for (String kItem : kItems) {
                def containsItem = kItem.contains(nameItem)
                //println("kItem: $kItem --- nameItem:$nameItem --- contains:$containsItem ")
                if (containsItem && params.get(kItem)) {
                    requireItem = true
                    break
                }
            }
        }

        return requireItem;
    }

}
