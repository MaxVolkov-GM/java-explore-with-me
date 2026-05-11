package ru.practicum.ewm.common;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@EqualsAndHashCode
public class OffsetPageRequest implements Pageable {
	private final int limit;
	private final int offset;
	private final Sort sort;

	public OffsetPageRequest(int offset, int limit) {
		this(offset, limit, Sort.unsorted());
	}

	public OffsetPageRequest(int offset, int limit, Sort sort) {
		if (offset < 0) {
			throw new IllegalArgumentException("Offset must not be less than zero");
		}

		if (limit < 1) {
			throw new IllegalArgumentException("Limit must be greater than zero");
		}

		this.limit = limit;
		this.offset = offset;
		this.sort = sort == null ? Sort.unsorted() : sort;
	}

	@Override
	public int getPageNumber() {
		return offset / limit;
	}

	@Override
	public int getPageSize() {
		return limit;
	}

	@Override
	public long getOffset() {
		return offset;
	}

	@Override
	public Pageable next() {
		return new OffsetPageRequest(offset + limit, limit, sort);
	}

	@Override
	public Pageable previousOrFirst() {
		return hasPrevious() ? new OffsetPageRequest(offset - limit, limit, sort) : first();
	}

	@Override
	public Pageable first() {
		return new OffsetPageRequest(0, limit, sort);
	}

	@Override
	public Pageable withPage(int pageNumber) {
		return new OffsetPageRequest(pageNumber * limit, limit, sort);
	}

	@Override
	public boolean hasPrevious() {
		return offset > 0;
	}
}