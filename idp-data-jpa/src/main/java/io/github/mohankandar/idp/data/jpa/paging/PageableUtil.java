package io.github.mohankandar.idp.data.jpa.paging;

import io.github.mohankandar.idp.core.paging.PageRequest;
import io.github.mohankandar.idp.core.paging.SortOrder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageableUtil {

    public Pageable toPageable(PageRequest req) {
        if (req == null) {
            return org.springframework.data.domain.PageRequest.of(0, 10);
        }

        Sort sort = Sort.unsorted();
        if (req.getSort() != null && !req.getSort().isEmpty()) {
            Sort.Order[] orders = req.getSort().stream()
                    .map(o -> new Sort.Order(
                            toSpringDirection(o),
                            resolveProperty(o)))
                    .toArray(Sort.Order[]::new);
            sort = Sort.by(orders);
        }

        return org.springframework.data.domain.PageRequest.of(req.getPage(), req.getSize(), sort);
    }

    private Sort.Direction toSpringDirection(SortOrder o) {
        if (o == null) {
            return Sort.Direction.ASC;
        }

        return o.getDirection() == SortOrder.Direction.DESC
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
    }

    private String resolveProperty(SortOrder o) {
        return (o == null) ? "id" : o.getField();
    }
}
