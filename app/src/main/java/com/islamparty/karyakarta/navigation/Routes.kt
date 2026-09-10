package com.islamparty.karyakarta.navigation

object Routes {
    const val LOGIN = "login"
    const val WORKER_LIST = "worker_list"
    const val WORKER_DETAIL = "worker_detail/{workerId}"
    const val WORKER_FORM = "worker_form?workerId={workerId}"
    const val MESSAGING = "messaging?workerIds={workerIds}"
    const val BUSINESS_CARD = "business_card/{workerId}"

    fun workerDetail(id: String) = "worker_detail/$id"
    fun workerFormNew() = "worker_form"
    fun workerFormEdit(id: String) = "worker_form?workerId=$id"
    fun messaging(workerIds: List<String>) = "messaging?workerIds=${workerIds.joinToString(",")}"
    fun messagingAll() = "messaging"
    fun businessCard(id: String) = "business_card/$id"
}
