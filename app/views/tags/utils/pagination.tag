<div class="row">
    <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
        <div class="text-center">
            %{
                pages = ext.PaginationExtensions.buildPossiblePages(_currentPage, _total);
            }%
            #{if pages.size() > 1}
            #{list items:pages, as:'page'}
                <a class="btn-bezel primary #{if page == _currentPage} active #{/if}" href="${ext.PaginationExtensions.buildUrl(_url, _params,page)}">${page}</a>
            #{/list}
            #{/if}
        </div>
    </div>
</div>