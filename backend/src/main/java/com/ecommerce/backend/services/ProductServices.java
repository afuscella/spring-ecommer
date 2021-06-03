package com.ecommerce.backend.services;

import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.backend.entities.Product;
import com.ecommerce.backend.exceptions.DatabaseIntegrityException;
import com.ecommerce.backend.exceptions.ResourceNotFoundException;
import com.ecommerce.backend.models.dto.ProductDTO;
import com.ecommerce.backend.models.dto.TransformProductDTO;
import com.ecommerce.backend.models.response.ProductResponse;
import com.ecommerce.backend.repositories.ProductRepository;

@Service
public class ProductServices {

	@Autowired
	ProductRepository productRepository;

	@Autowired
	TransformProductDTO transformProductDTO;

	@Transactional(readOnly = true)
	public ProductResponse handleAllPaged(PageRequest pageRequest) {
		Page<Product> products = productRepository.findAll(pageRequest);

		Page<ProductDTO> data = products.map(product -> new ProductDTO(product, product.getCategories()));
		return new ProductResponse(data);
	}

	@Transactional(readOnly = true)
	public ProductDTO handleIndex(UUID uuid) {
		Optional<Product> productOptional = productRepository.findById(uuid);
		Product entity = productOptional.orElseThrow(ResourceNotFoundException::new);

		return new ProductDTO(entity, entity.getCategories());
	}

	@Transactional
	public ProductDTO handleCreate(ProductDTO productDTO) {
		Product entity = transformProductDTO.toEntity(productDTO);
		entity = productRepository.save(entity);
		return new ProductDTO(entity, entity.getCategories());
	}

	@Transactional
	public ProductDTO handleUpdateByIndex(UUID uuid, ProductDTO productDTO) {
		try {
			Product entity = productRepository.getOne(uuid);
			entity = transformProductDTO.toEntity(productDTO, entity);

			entity = productRepository.save(entity);
			return new ProductDTO(entity, entity.getCategories());
		}
		catch (EntityNotFoundException e) {
			throw new ResourceNotFoundException();
		}
	}

	public void handleDeleteByIndex(UUID uuid) {
		try {
			productRepository.deleteById(uuid);
		}
		catch (EmptyResultDataAccessException e) {
			throw new ResourceNotFoundException();
		}
		catch (DataIntegrityViolationException e) {
			throw new DatabaseIntegrityException();
		}
	}

}
