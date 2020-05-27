package com.example.purchase

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import java.lang.Exception
import java.security.spec.ECField

class ShoppinAdapter(private val mContext: Context,
                     private val viewResourceId: Int,
                     private val items: ArrayList<ShoppinList>):
    ArrayAdapter<ShoppinList?>(mContext, viewResourceId, items.toList()){

    private val itemsAll = items.clone() as ArrayList<ShoppinList>
    private var suggestions = ArrayList<ShoppinList>()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v: View? = convertView

        if (v == null) {
            val vi = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            v = vi.inflate(viewResourceId, null)
        }

        val shoppin: ShoppinList? = getItem(position)
        if (shoppin != null) {
            val annotName = v?.findViewById(R.id.annotName) as TextView?
            val annotCost = v?.findViewById(R.id.annotCost) as TextView?
            annotName?.text = shoppin.annotName
            annotCost?.text = shoppin.annotCost
        }

        return v!!
    }

    override fun getFilter(): Filter {
        return nameFilter
    }

    private var nameFilter: Filter = object : Filter() {
        override fun convertResultToString(resultValue: Any): String {
            return (resultValue as ShoppinList).annotName.toString()
        }

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            return if (constraint != null) {
                suggestions.clear()

                for (shoppin in itemsAll) {
                    if (shoppin.annotName.toString().toLowerCase().startsWith(constraint.toString().toLowerCase())) {
                        suggestions.add(shoppin)
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = suggestions
                filterResults.count = suggestions.size
                filterResults
            } else {
                FilterResults()
            }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            try{
                val filteredList =  results?.values as ArrayList<ShoppinList>?

                if (results != null && results.count > 0) {
                    clear()
                    for (c: ShoppinList in filteredList ?: listOf<ShoppinList>()) {
                        add(c)
                    }
                    notifyDataSetChanged()
                }
            }catch (e: Exception){
                Log.i("Tester",e.toString())
            }
        }
    }
}