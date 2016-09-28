#{if controllers.Changelog.showUserChangelog()}
    %{changelog = controllers.Changelog.getLastChangelog()}%
    #{if !changelog.isEmpty() }
        %{ version = changelog.get(0).getVersion() }%
        <div class="modal" id="changelog-modal" data-ignore="false">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                        <h4 class="modal-title">&{'changelog.version', version }</h4>
                    </div>
                    <div class="modal-body">
                        <p>&{'changelog.description'}</p>
                        <ul>
                            #{list items:changelog, as:'changelogPoint'}
                                <li>${changelogPoint.getTitle(lang)}</li>
                            #{/list}
                        </ul>
                        <p>&{'changelog.link', play.mvc.Router.reverse("Changelog.userChangelog").url }</p>
                    </div>
                    <div class="modal-footer">
                        <button id="ignore-changelog" type="button" class="btn btn-default"  data-dismiss="modal">&{'changelog.ignore'}</button>
                        <button type="button" class="btn btn-primary" data-dismiss="modal">&{'changelog.close'}</button>
                    </div>
                </div>
            </div>
        </div>
        <script>
            $('#changelog-modal').modal('show');
            $('#ignore-changelog').click(function () {
                $('#changelog-modal').data('ignore', 'true')
            });
            $('#changelog-modal').on('hidden.bs.modal', function () {
                if ($(this).data('ignore')) {
                    $.post("@{Changelog.changelogRead(true)}", {authenticityToken:"${session.getAuthenticityToken()}"});
                } else {
                    $.post("@{Changelog.changelogRead(false)}", {authenticityToken:"${session.getAuthenticityToken()}"});
                }
            });
        </script>
    #{/if}
#{/if}