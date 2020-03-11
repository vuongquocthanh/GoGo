package ds.vuongquocthanh.gogo.mvp.presenter

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import ds.vuongquocthanh.gogo.api.ApiUtil
import ds.vuongquocthanh.gogo.mvp.Presenter
import ds.vuongquocthanh.gogo.mvp.View
import ds.vuongquocthanh.gogo.mvp.model.googledirection.GoogleDirection
import ds.vuongquocthanh.gogo.mvp.view.DirectionViewPresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class DirectionPresenter :Presenter{
    private val compositeDisposable = CompositeDisposable()
    private lateinit var viewPresenter : DirectionViewPresenter
    override fun attachView(view: View) {
        viewPresenter = view as DirectionViewPresenter
    }

    override fun dispose() {
        compositeDisposable.dispose()
    }

     fun getDirection(startLocation : String, endLocation : String ,sensor : String,mode : String, key : String){
        compositeDisposable.add(ApiUtil.getAPIGoogle().getDirection(startLocation,endLocation,sensor,mode, key)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::getDataSuccess) {t -> getDataFail(t,"Get Direction Fail")})
    }

    private fun getDataSuccess(response : GoogleDirection ){
        viewPresenter.getDirectionResponse(response)
    }

    private fun getDataFail(t:Throwable,error:String){
        Log.d("getDirectionFail",t.localizedMessage)
        viewPresenter.showError(error)
    }

}