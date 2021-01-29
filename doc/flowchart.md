[![](https://mermaid.ink/img/eyJjb2RlIjoiZmxvd2NoYXJ0IFRCICAgIFxuICAgIHN1YmdyYXBoIDFbXCJ0ZXJyYWZvcm0gcHJvdmlkZXJcIl1cbiAgICBzdGFydFtcImdpdCBjb21taXRcIl0tLSBnaXRob29rLS0-Vk1zXG4gICAgZW5kXG4gICAgc3ViZ3JhcGggYW5zaWJsZVxuICAgIGJbXCJDb25uZWN0cyB0byBlYWNoIFZNL05vZGVcIl0tLSBzc2ggLS0-IGIyW1wiaW5zdGFsbHMgc29mdHdhcmVcIl1cbiAgICBlbmRcbiAgICAxW1widGVycmFmb3JtIHByb3ZpZGVyXCJdIC0tPiBhbnNpYmxlICAgIFxuICAgICIsIm1lcm1haWQiOnsidGhlbWUiOiJkZWZhdWx0In0sInVwZGF0ZUVkaXRvciI6ZmFsc2V9)](https://mermaid-js.github.io/mermaid-live-editor/#/edit/eyJjb2RlIjoiZmxvd2NoYXJ0IFRCICAgIFxuICAgIHN1YmdyYXBoIDFbXCJ0ZXJyYWZvcm0gcHJvdmlkZXJcIl1cbiAgICBzdGFydFtcImdpdCBjb21taXRcIl0tLSBnaXRob29rLS0-Vk1zXG4gICAgZW5kXG4gICAgc3ViZ3JhcGggYW5zaWJsZVxuICAgIGJbXCJDb25uZWN0cyB0byBlYWNoIFZNL05vZGVcIl0tLSBzc2ggLS0-IGIyW1wiaW5zdGFsbHMgc29mdHdhcmVcIl1cbiAgICBlbmRcbiAgICAxW1widGVycmFmb3JtIHByb3ZpZGVyXCJdIC0tPiBhbnNpYmxlICAgIFxuICAgICIsIm1lcm1haWQiOnsidGhlbWUiOiJkZWZhdWx0In0sInVwZGF0ZUVkaXRvciI6ZmFsc2V9)

```
flowchart TB    
    subgraph 1["terraform provider"]
    start["git commit"]-- githook-->VMs
    end
    subgraph ansible
    b["Connects to each VM/Node"]-- ssh --> b2["installs software"]
    end
    1["terraform provider"] --> ansible          
```