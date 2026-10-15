package es.biobuddi.fillet;

import android.app.assist.AssistStructure.ViewNode;
import android.os.CancellationSignal;
import android.service.autofill.AutofillService;
import android.service.autofill.Dataset;
import android.service.autofill.FillCallback;
import android.service.autofill.FillRequest;
import android.service.autofill.FillResponse;
import android.service.autofill.Presentations;
import android.service.autofill.SaveCallback;
import android.service.autofill.SaveRequest;
import android.view.View;
import android.view.ViewStructure.HtmlInfo;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import android.widget.RemoteViews;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class FillService extends AutofillService {

    static Stream<ViewNode> flatten(ViewNode node) {
        return Stream.concat(
            Stream.of(node),
            IntStream.range(0, node.getChildCount())
                .mapToObj(node::getChildAt)
                .flatMap(FillService::flatten)
        );
    }

    static List<String> candidates(ViewNode node) {
        var attributes = Optional.ofNullable(node.getHtmlInfo())
            .map(HtmlInfo::getAttributes)
            .orElse(List.of());
        return Stream.of(
            Optional.ofNullable(node.getAutofillHints()).stream().flatMap(Arrays::stream),
            Stream.of("autocomplete", "name", "id").flatMap(key ->
                attributes
                    .stream()
                    .filter(pair -> key.equals(pair.first))
                    .map(pair -> pair.second)
            ),
            Stream.of(node.getIdEntry(), Objects.toString(node.getHint(), null))
        )
            .flatMap(stream -> stream)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public void onFillRequest(
        FillRequest request,
        CancellationSignal signal,
        FillCallback callback
    ) {
        var contexts = request.getFillContexts();
        var structure = contexts.get(contexts.size() - 1).getStructure();
        var nodes = IntStream.range(0, structure.getWindowNodeCount())
            .mapToObj(index -> structure.getWindowNodeAt(index).getRootViewNode())
            .flatMap(FillService::flatten)
            .collect(Collectors.toList());
        var web = nodes
            .stream()
            .filter(node -> node.getWebDomain() != null)
            .findFirst();
        var target = Profile.target(
            structure.getActivityComponent().getPackageName(),
            web.map(ViewNode::getWebScheme).orElse(null),
            web.map(ViewNode::getWebDomain).orElse(null)
        );
        Map<AutofillId, Field> fields = nodes
            .stream()
            .filter(
                node ->
                    node.getAutofillId() != null &&
                    node.getAutofillType() == View.AUTOFILL_TYPE_TEXT
            )
            .flatMap(node ->
                Field.classify(candidates(node))
                    .map(field -> Map.entry(node.getAutofillId(), field))
                    .stream()
            )
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        var datasets = Profile.ALL.stream()
            .filter(profile -> profile.associates(target))
            .filter(profile -> fields.values().stream().anyMatch(profile.values()::containsKey))
            .map(profile -> {
                var menu = new RemoteViews(getPackageName(), android.R.layout.simple_list_item_1);
                menu.setTextViewText(android.R.id.text1, profile.label());
                var dataset = new Dataset.Builder(
                    new Presentations.Builder().setMenuPresentation(menu).build()
                );
                fields.forEach((id, field) ->
                    Optional.ofNullable(profile.values().get(field)).ifPresent(value ->
                        dataset.setField(
                            id,
                            new android.service.autofill.Field.Builder()
                                .setValue(AutofillValue.forText(value))
                                .build()
                        )
                    )
                );
                return dataset.build();
            })
            .collect(Collectors.toList());
        if (datasets.isEmpty()) {
            callback.onSuccess(null);
            return;
        }
        var response = new FillResponse.Builder();
        datasets.forEach(response::addDataset);
        callback.onSuccess(response.build());
    }

    @Override
    public void onSaveRequest(SaveRequest request, SaveCallback callback) {
        callback.onFailure("Fillet saves nothing until data entry arrives");
    }
}
