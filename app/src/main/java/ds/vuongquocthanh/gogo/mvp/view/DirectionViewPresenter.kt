package ds.vuongquocthanh.gogo.mvp.view

import ds.vuongquocthanh.gogo.mvp.View
import ds.vuongquocthanh.gogo.mvp.model.googledirection.GoogleDirection

interface DirectionViewPresenter : View{
    fun getDirectionResponse(direction : GoogleDirection)
}