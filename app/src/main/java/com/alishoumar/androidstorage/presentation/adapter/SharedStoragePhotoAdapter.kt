package com.alishoumar.androidstorage.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.Coil
import coil.load
import com.alishoumar.androidstorage.domain.models.ExternalStoragePhoto
import com.alishoumar.androidstorage.databinding.ItemPhotoBinding
import com.alishoumar.androidstorage.presentation.adapter.InternalStoragePhotoAdapter.InternalStoragePhotoViewHolder

class SharedStoragePhotoAdapter(
    val onPhotoClick:(ExternalStoragePhoto) -> Unit,
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
    ) : RecyclerView.ViewHolder(binding.root)

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
        val currentPhoto = currentList[position]

        holder.binding.apply {

            ivPhoto.load(currentPhoto.uri) {
                crossfade(true)
                size(500, 500)
            }

            val aspectRatio = currentPhoto.width.toFloat() / currentPhoto.height.toFloat()
            ConstraintSet().apply {
                clone(root)
                setDimensionRatio(ivPhoto.id, aspectRatio.toString())
                applyTo(root)
            }

            ivPhoto.setOnLongClickListener {
                onPhotoClick(currentPhoto)
                true
            }

            ivPhoto.setOnClickListener {
                onImageClick(currentPhoto)
                true
            }
        }
    }



}