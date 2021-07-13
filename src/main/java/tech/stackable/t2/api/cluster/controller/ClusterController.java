package tech.stackable.t2.api.cluster.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tech.stackable.t2.api.cluster.YamlClusterDefinitionReader;
import tech.stackable.t2.api.cluster.domain.Cluster;
import tech.stackable.t2.api.cluster.service.TerraformAnsibleClusterService;
import tech.stackable.t2.security.SecurityToken;
import tech.stackable.t2.security.TokenIncorrectException;
import tech.stackable.t2.security.TokenRequiredException;

import java.util.Collection;
import java.util.UUID;

@RestController
@RequestMapping("api/clusters")
public class ClusterController {

    @Autowired
    private TerraformAnsibleClusterService clusterService;

    @Autowired
    private SecurityToken requiredToken;

    @GetMapping()
    @ResponseBody
    @Operation(summary = "Get all clusters", description = "Get list of all active clusters")
    public Collection<Cluster> getClusters(@RequestHeader(name = "t2-token", required = false) String token) {
        checkToken(token);
        return clusterService.getAllClusters();
    }

    @GetMapping("{id}")
    @ResponseBody
    @Operation(summary = "Get cluster", description = "Gets the specified cluster")
    public Cluster getCluster(
            @Parameter(name = "id", description = "ID (UUID) of the cluster") @PathVariable(name = "id", required = true) UUID id,
            @RequestHeader(name = "t2-token", required = false) String token) {
        checkToken(token);
        Cluster cluster = clusterService.getCluster(id);
        if (cluster == null) {
            throw new ClusterNotFoundException(String.format("No cluster found with id '%s'.", id));
        }
        return cluster;
    }

    @PostMapping(consumes = { "application/json", "application/yaml" })
    @ResponseBody
    @Operation(summary = "Creates a new cluster", description = "Creates a new cluster and starts it")
    public Cluster createCluster(
            @RequestHeader(name = "t2-token", required = false) String token,
            @RequestBody(required = true) String clusterDefinition) {

        checkToken(token);
        return clusterService.createCluster(new YamlClusterDefinitionReader().convert(clusterDefinition));
    }

    @DeleteMapping("{id}")
    @ResponseBody
    @Operation(summary = "Deletes a cluster", description = "Deletes the specified cluster")
    public Cluster deleteCluster(
            @Parameter(name = "id", description = "ID (UUID) of the cluster") @PathVariable(name = "id", required = true) UUID id,
            @RequestHeader(name = "t2-token", required = false) String token) {
        checkToken(token);
        Cluster cluster = clusterService.deleteCluster(id);
        if (cluster == null) {
            throw new ClusterNotFoundException(String.format("No cluster found with id '%s'.", id));
        }
        return cluster;
    }

    @GetMapping("{id}/wireguard-config/{index}")
    @ResponseBody
    @Operation(summary = "read wireguard config", description = "Gets the wireguard client config with the specified index")
    public String getWireguardConfig(
            @Parameter(name = "id", description = "ID (UUID) of the cluster") @PathVariable(name = "id", required = true) UUID id,
            @Parameter(name = "index", description = "index of the wireguard client config") @PathVariable(name = "index", required = true) int index,
            @RequestHeader(name = "t2-token", required = false) String token) {
        checkToken(token);
        Cluster cluster = clusterService.getCluster(id);
        if (cluster == null) {
            throw new ClusterNotFoundException(String.format("No cluster found with id '%s'.", id));
        }
        String wireguardClientConfig = this.clusterService.getWireguardClientConfig(id, index);
        if (wireguardClientConfig == null) {
            throw new ClusterNotFoundException(String.format("No wireguard config[%d] found for cluster with id '%s'.", index, id));
        }
        return wireguardClientConfig;
    }

    @GetMapping("{id}/stackable-client-script")
    @ResponseBody
    @Operation(summary = "read Stackable client script", description = "Reads the client script to work with the cluster")
    public String getClientScript(
            @Parameter(name = "id", description = "ID (UUID) of the cluster") @PathVariable(name = "id", required = true) UUID id,
            @RequestHeader(name = "t2-token", required = false) String token) {
        checkToken(token);
        Cluster cluster = clusterService.getCluster(id);
        if (cluster == null) {
            throw new ClusterNotFoundException(String.format("No cluster found with id '%s'.", id));
        }
        String clientScript = this.clusterService.getClientScript(id);
        if (clientScript == null) {
            throw new ClusterNotFoundException(String.format("No Stackable client script found for cluster with id '%s'.", id));
        }
        return clientScript;
    }

    @GetMapping("{id}/stackable-versions")
    @ResponseBody
    @Operation(summary = "read Stackable version information document", description = "Reads a text document which contains version information on installed Stackable components")
    public String getStackableVersions(
            @Parameter(name = "id", description = "ID (UUID) of the cluster") @PathVariable(name = "id", required = true) UUID id,
            @RequestHeader(name = "t2-token", required = false) String token) {
        checkToken(token);
        Cluster cluster = clusterService.getCluster(id);
        if (cluster == null) {
            throw new ClusterNotFoundException(String.format("No cluster found with id '%s'.", id));
        }
        String stackableVersions = this.clusterService.getVersionInformation(id);
        if (stackableVersions == null) {
            throw new ClusterNotFoundException(String.format("No Stackable version information document found for cluster with id '%s'.", id));
        }
        return stackableVersions;
    }

    @GetMapping("{id}/kubeconfig")
    @ResponseBody
    @Operation(summary = "read Kubeconfig", description = "Reads a Kubeconfig file to work with the cluster while working with VPN")
    public String getKubeconfig(
            @Parameter(name = "id", description = "ID (UUID) of the cluster") @PathVariable(name = "id", required = true) UUID id,
            @RequestHeader(name = "t2-token", required = false) String token) {
        checkToken(token);
        Cluster cluster = clusterService.getCluster(id);
        if (cluster == null) {
            throw new ClusterNotFoundException(String.format("No cluster found with id '%s'.", id));
        }
        String kubeconfig = this.clusterService.getKubeconfigFile(id);
        if (kubeconfig == null) {
            throw new ClusterNotFoundException(String.format("No Kubeconfig found for cluster with id '%s'.", id));
        }
        return kubeconfig;
    }

    @GetMapping("{id}/log")
    @ResponseBody
    @Operation(summary = "read logs", description = "Reads the logs for the given cluster")
    public String getLog (
            @Parameter(name = "id", description = "ID (UUID) of the cluster") @PathVariable(name = "id", required = true) UUID id,
            @RequestHeader(name = "t2-token", required = false) String token) {
        checkToken(token);
        Cluster cluster = clusterService.getCluster(id);
        if (cluster == null) {
            throw new ClusterNotFoundException(String.format("No cluster found with id '%s'.", id));
        }
        return this.clusterService.getLogs(id);
    }

    /**
     * Checks if the given token is valid, throws appropriate exception otherwise
     * 
     * @param token token
     */
    private void checkToken(String token) {
        if (token == null) {
            throw new TokenRequiredException();
        }
        if (!this.requiredToken.isOk(token)) {
            throw new TokenIncorrectException();
        }
    }

}
