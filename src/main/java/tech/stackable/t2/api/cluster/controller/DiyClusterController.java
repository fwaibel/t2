package tech.stackable.t2.api.cluster.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import tech.stackable.t2.api.cluster.YamlClusterDefinitionReader;
import tech.stackable.t2.api.cluster.service.TerraformAnsibleClusterService;

@RestController
@RequestMapping("api/diy-cluster")
public class DiyClusterController {

    @Autowired
    private TerraformAnsibleClusterService clusterService;

    @GetMapping(consumes = { "application/json",
            "application/yaml" }, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    @Operation(summary = "Get DIY cluster package", description = "Creates a DIY cluster package (ZIP)")
    public byte[] createCluster(@RequestBody(required = true) String clusterDefinition) {
        return clusterService.createDiyCluster(new YamlClusterDefinitionReader().convert(clusterDefinition));
    }

}
