package com.example.myproject

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.myproject.databinding.RowImagesPickedBinding

class AdapterImagePicked(
    private val context: Context,
    private val imagesPickedArrayList: ArrayList<ModelImagePicked>
) : Adapter<AdapterImagePicked.HolderImagePicked>() {

    private lateinit var binding: RowImagesPickedBinding

    private companion object {
        private const val TAG = "IMAGES_TAG"
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderImagePicked {

        val inflater = LayoutInflater.from(context)
        binding = RowImagesPickedBinding.inflate(inflater, parent, false)
        return HolderImagePicked(binding.root)

    }

    override fun onBindViewHolder(holder: HolderImagePicked, position: Int) {

        val model = imagesPickedArrayList[position]

        val imageUri = model.imageUri
        Log.d(TAG, "onBindViewHolder: imageUri $imageUri")

        try {
            Glide.with(context)
                .load(imageUri)
                .placeholder(R.drawable.ic_image_gray)
                .into(holder.imageIv)
        } catch (e: Exception) {
            Log.e(TAG, "onBindViewHolder: ", e)
        }

        holder.closeBtn.setOnClickListener {
            imagesPickedArrayList.remove(model)
            notifyDataSetChanged()

        }

    }

    override fun getItemCount(): Int {

        return imagesPickedArrayList.size

    }

    inner class HolderImagePicked(itemView: View) : ViewHolder(itemView) {

        var imageIv = binding.imageIv
        var closeBtn = binding.closeBtn

    }
}