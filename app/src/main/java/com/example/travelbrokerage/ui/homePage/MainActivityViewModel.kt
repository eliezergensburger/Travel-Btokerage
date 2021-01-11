package com.example.travelbrokerage.ui.homePage

import android.content.Context.MODE_PRIVATE
import androidx.lifecycle.*
import com.example.travelbrokerage.data.models.Travel
import com.example.travelbrokerage.data.models.Travel.RequestType
import com.example.travelbrokerage.data.repositories.ITravelRepository
import com.example.travelbrokerage.data.repositories.TravelRepository
import com.example.travelbrokerage.util.MyApplication

const val MAX_DISTANCE = 20

// Represent the View Model of AddTravelActivity
class MainActivityViewModel : ViewModel() {

    private var travelsList: List<Travel> = ArrayList<Travel>()
    private var costumerList: MutableLiveData<List<Travel>> = MutableLiveData()
    private var companyList: MutableLiveData<List<Travel>> = MutableLiveData()
    private var historyList: MutableLiveData<List<Travel>> = MutableLiveData()
    private val sharedPreferences =
        MyApplication.getAppContext().getSharedPreferences("USER", MODE_PRIVATE)
    private val userMail = sharedPreferences.getString("Mail", "")

    private var travelRepo: ITravelRepository = TravelRepository()

    init {
        travelRepo.setNotifyToTravelListListener {
            travelsList = travelRepo.allTravels
            costumerList.value = filterCostumerTravels(userMail!!)
            companyList.value = filterCompanyTravels()
            //historyList.value = filterHistoryTravels(userMail!!)
        }


    }

    //
    fun getCostumerTravels(): LiveData<List<Travel>> = costumerList

    fun getCompanyTravels(): LiveData<List<Travel>> = companyList

    fun getHistoryTravels(): LiveData<List<Travel>> = historyList

    fun loadHistoryList() {
        travelsList = travelRepo.loadData()
        historyList.value = travelsList
    }

    fun loadCompanyList() {
        travelsList = travelRepo.loadData()
        companyList.value = filterCompanyTravels()
    }

    fun loadCostumerList() {
        travelsList = travelRepo.loadData()
        costumerList.value = filterCostumerTravels(userMail!!)
    }

    // Add travel obj to the DataBase
    fun addTravel(travel: Travel) {
        travelRepo.addTravel(travel)
    }

    // Get the boolean value Which indicates whether the value
    // was successfully inserted into the database
    fun getIsSuccess(): LiveData<Boolean> {
        return travelRepo.isSuccess
    }

    private fun filterCostumerTravels(userMail: String): List<Travel> {
        val tempList = ArrayList<Travel>()
        for (travel in travelsList) {
            if (travel.clientEmail == userMail && (travel.requestType != RequestType.CLOSE &&
                        travel.requestType != RequestType.PAYMENT)
            ) {
                tempList.add(travel)
            }
        }
        return tempList
    }

    private fun filterCompanyTravels(): List<Travel> {
        val tempList = ArrayList<Travel>()
        val companyMail = userMail!!.substringBefore('@')

        for (travel in travelsList) {
            if (travel.requestType == RequestType.SENT) {
                val dis = MainActivity.calculateDistance(travel.address!!)
                if (dis < MAX_DISTANCE)
                    tempList.add(travel)
            } else if (travel.requestType != RequestType.SENT && travel.requestType != RequestType.PAYMENT) {
                if (travel.companyEmail == companyMail){
                    tempList.add(travel)
                }
            }
        }
        return tempList
    }

    fun updateTravel(currentItem: Travel) {
        travelRepo.updateTravel(currentItem)
    }
}