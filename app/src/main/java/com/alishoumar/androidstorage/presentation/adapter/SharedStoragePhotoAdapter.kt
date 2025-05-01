package com.alishoumar.androidstorage.presentation.adapter

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.Coil
import coil.load
import coil.request.CachePolicy
import coil.transform.CircleCropTransformation
import com.alishoumar.androidstorage.R
import com.alishoumar.androidstorage.domain.models.ExternalStoragePhoto
import com.alishoumar.androidstorage.databinding.ItemPhotoBinding
import com.alishoumar.androidstorage.presentation.adapter.InternalStoragePhotoAdapter.InternalStoragePhotoViewHolder

class SharedStoragePhotoAdapter(
    val lifeCycleOwner: LifecycleOwner,
    val onImageClick:(ExternalStoragePhoto) -> Unit
) : ListAdapter<ExternalStoragePhoto, SharedStoragePhotoAdapter.SharedStoragePhotoViewHolder>(
    Companion
) {


    companion object : DiffUtil.ItemCallback<ExternalStoragePhoto>(){
        override fun areItemsTheSame(
            oldItem: ExternalStoragePhoto,
            newItem: ExternalStoragePhoto
        ): Boolean {
           return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: ExternalStoragePhoto,
            newItem: ExternalStoragePhoto
        ): Boolean {
            return oldItem == newItem
        }
    }

    inner class SharedStoragePhotoViewHolder(
        val binding: ItemPhotoBinding
    ) : RecyclerView.ViewHolder(binding.root){

        private var currentPhoto: ExternalStoragePhoto? = null

        init {
            binding.ivPhoto.setOnClickListener {
                currentPhoto?.let { onImageClick(it) }
            }
        }

        fun bind(photo: ExternalStoragePhoto) {
            currentPhoto = photo

                binding.ivPhoto.load(photo.uri) {
                    lifecycle(lifeCycleOwner)
                    crossfade(true)
                    placeholder(R.drawable.baseline_photo_24)
                    size(300)
                    diskCachePolicy(CachePolicy.ENABLED)
                    memoryCachePolicy(CachePolicy.ENABLED)
                }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SharedStoragePhotoViewHolder {
        return SharedStoragePhotoViewHolder(
            ItemPhotoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }
    override fun onBindViewHolder(holder: SharedStoragePhotoViewHolder, position: Int) {
        holder.binding.apply {
            holder.bind(getItem(position))
        }
    }
}